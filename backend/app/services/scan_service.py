import json
from datetime import datetime, timedelta, timezone

from fastapi import HTTPException, status
from pydantic import BaseModel, ConfigDict, Field, ValidationError
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.services.points_service import get_points_balance_summary

TEMP_QR_MAX_AGE = timedelta(minutes=2)
TEMP_QR_FUTURE_TOLERANCE = timedelta(seconds=30)


class TemporaryOfflineQrPayload(BaseModel):
    model_config = ConfigDict(extra="forbid")

    v: int = Field(ge=1)
    bin_code: str = Field(min_length=1, max_length=255)
    item_count: int = Field(gt=0)
    ts: datetime


def claim_scan_from_qr(db: Session, current_user: dict, qr_raw: str) -> dict:
    payload = _parse_qr_payload(qr_raw)
    bin_record = _get_bin_by_code(db, payload.bin_code)
    scan_at = _normalize_timestamp(payload.ts)
    now = _utc_now_naive()

    if scan_at > now + TEMP_QR_FUTURE_TOLERANCE:
        return _create_invalid_scan_response(
            db=db,
            user_id=current_user["user_id"],
            bin_record=bin_record,
            scan_at=scan_at,
            item_count=payload.item_count,
            invalid_reason="This QR code has an invalid timestamp."
        )

    if now - scan_at > TEMP_QR_MAX_AGE:
        return _create_invalid_scan_response(
            db=db,
            user_id=current_user["user_id"],
            bin_record=bin_record,
            scan_at=scan_at,
            item_count=payload.item_count,
            invalid_reason="This QR code has expired. Please scan a fresh QR code."
        )

    if _has_existing_valid_claim(db, bin_record["bin_id"], scan_at):
        return _create_invalid_scan_response(
            db=db,
            user_id=current_user["user_id"],
            bin_record=bin_record,
            scan_at=scan_at,
            item_count=payload.item_count,
            invalid_reason="This QR code has already been claimed."
        )

    scan_row = db.execute(
        text("""
            INSERT INTO scan_event (
                user_id,
                bin_id,
                scan_at,
                is_valid,
                invalid_reason,
                total_points_awarded
            )
            VALUES (
                :user_id,
                :bin_id,
                :scan_at,
                TRUE,
                NULL,
                :points_awarded
            )
            RETURNING scan_id, bin_id, scan_at, is_valid, invalid_reason, total_points_awarded;
        """),
        {
            "user_id": current_user["user_id"],
            "bin_id": bin_record["bin_id"],
            "scan_at": scan_at,
            "points_awarded": payload.item_count
        }
    ).fetchone()

    db.execute(
        text("""
            INSERT INTO points_txn (
                user_id,
                scan_id,
                type,
                points,
                created_at
            )
            VALUES (
                :user_id,
                :scan_id,
                'EARN',
                :points,
                NOW()
            );
        """),
        {
            "user_id": current_user["user_id"],
            "scan_id": scan_row[0],
            "points": payload.item_count
        }
    )
    db.commit()

    balance = get_points_balance_summary(db, current_user["user_id"])

    return {
        "message": "QR code claimed successfully",
        "scan": {
            "scan_id": scan_row[0],
            "bin_id": scan_row[1],
            "bin_code": bin_record["public_code"],
            "item_count": payload.item_count,
            "points_awarded": scan_row[5] or 0,
            "scan_at": scan_row[2],
            "is_valid": scan_row[3],
            "invalid_reason": scan_row[4]
        },
        "current_points_balance": balance["current_points_balance"]
    }


def _parse_qr_payload(qr_raw: str) -> TemporaryOfflineQrPayload:
    try:
        payload_data = json.loads(qr_raw)
    except json.JSONDecodeError as exc:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="QR code payload is not valid JSON"
        ) from exc

    try:
        return TemporaryOfflineQrPayload.model_validate(payload_data)
    except ValidationError as exc:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"QR code payload is invalid: {exc.errors()[0]['msg']}"
        ) from exc


def _get_bin_by_code(db: Session, bin_code: str) -> dict:
    row = db.execute(
        text("""
            SELECT bin_id, public_code, is_active
            FROM bins
            WHERE public_code = :public_code;
        """),
        {"public_code": bin_code}
    ).fetchone()

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Bin not found for the scanned QR code"
        )

    if not row[2]:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="This bin is inactive and cannot accept scan claims"
        )

    return {
        "bin_id": row[0],
        "public_code": row[1],
        "is_active": row[2]
    }


def _has_existing_valid_claim(db: Session, bin_id: int, scan_at: datetime) -> bool:
    row = db.execute(
        text("""
            SELECT scan_id
            FROM scan_event
            WHERE bin_id = :bin_id
              AND scan_at = :scan_at
              AND is_valid = TRUE
            LIMIT 1;
        """),
        {
            "bin_id": bin_id,
            "scan_at": scan_at
        }
    ).fetchone()

    return row is not None


def _create_invalid_scan_response(
    db: Session,
    user_id: int,
    bin_record: dict,
    scan_at: datetime,
    item_count: int,
    invalid_reason: str
) -> dict:
    row = db.execute(
        text("""
            INSERT INTO scan_event (
                user_id,
                bin_id,
                scan_at,
                is_valid,
                invalid_reason,
                total_points_awarded
            )
            VALUES (
                :user_id,
                :bin_id,
                :scan_at,
                FALSE,
                :invalid_reason,
                0
            )
            RETURNING scan_id, bin_id, scan_at, is_valid, invalid_reason, total_points_awarded;
        """),
        {
            "user_id": user_id,
            "bin_id": bin_record["bin_id"],
            "scan_at": scan_at,
            "invalid_reason": invalid_reason
        }
    ).fetchone()
    db.commit()

    balance = get_points_balance_summary(db, user_id)

    return {
        "message": "QR code claim rejected",
        "scan": {
            "scan_id": row[0],
            "bin_id": row[1],
            "bin_code": bin_record["public_code"],
            "item_count": item_count,
            "points_awarded": 0,
            "scan_at": row[2],
            "is_valid": row[3],
            "invalid_reason": row[4]
        },
        "current_points_balance": balance["current_points_balance"]
    }


def _normalize_timestamp(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value

    return value.astimezone(timezone.utc).replace(tzinfo=None)


def _utc_now_naive() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)
