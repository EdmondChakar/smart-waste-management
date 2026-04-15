from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.services.points_service import get_points_balance_summary

REQUESTED_REDEMPTION_STATUS = "REQUESTED"
FULFILLED_REDEMPTION_STATUS = "FULFILLED"
CANCELLED_REDEMPTION_STATUS = "CANCELLED"
ADMIN_ALLOWED_STATUS_UPDATES = {
    FULFILLED_REDEMPTION_STATUS,
    CANCELLED_REDEMPTION_STATUS
}


def redeem_reward_for_user(db: Session, user_id: int, reward_id: int) -> dict:
    reward = _get_reward_for_redemption(db, reward_id)
    balance = get_points_balance_summary(db, user_id)

    if balance["current_points_balance"] < reward["points_cost"]:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="You do not have enough points to redeem this reward"
        )

    requested_status = _get_redemption_status(db, REQUESTED_REDEMPTION_STATUS)

    redemption_row = db.execute(
        text("""
            INSERT INTO redemptions (
                user_id,
                reward_id,
                points_spent,
                status_id,
                voucher_code,
                requested_at,
                fulfilled_at
            )
            VALUES (
                :user_id,
                :reward_id,
                :points_spent,
                :status_id,
                NULL,
                NOW(),
                NULL
            )
            RETURNING redemption_id, requested_at, fulfilled_at;
        """),
        {
            "user_id": user_id,
            "reward_id": reward["reward_id"],
            "points_spent": reward["points_cost"],
            "status_id": requested_status["status_id"]
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
                NULL,
                'REDEEM',
                :points,
                NOW()
            );
        """),
        {
            "user_id": user_id,
            "points": -reward["points_cost"]
        }
    )
    db.commit()

    updated_balance = get_points_balance_summary(db, user_id)

    return {
        "message": "Reward redeemed successfully",
        "redemption": {
            "redemption_id": redemption_row[0],
            "reward_id": reward["reward_id"],
            "reward_title": reward["title"],
            "reward_description": reward["description"],
            "points_spent": reward["points_cost"],
            "status_id": requested_status["status_id"],
            "status_code": requested_status["status_code"],
            "voucher_code": None,
            "requested_at": redemption_row[1],
            "fulfilled_at": redemption_row[2]
        },
        "current_points_balance": updated_balance["current_points_balance"]
    }


def get_redemption_history(db: Session, user_id: int, limit: int = 20) -> dict:
    result = db.execute(
        text("""
            SELECT
                r.redemption_id,
                r.reward_id,
                rw.title,
                rw.description,
                r.points_spent,
                rs.status_id,
                rs.status_code,
                r.voucher_code,
                r.requested_at,
                r.fulfilled_at
            FROM redemptions r
            INNER JOIN rewards rw ON rw.reward_id = r.reward_id
            INNER JOIN redemption_statuses rs ON rs.status_id = r.status_id
            WHERE r.user_id = :user_id
            ORDER BY r.requested_at DESC, r.redemption_id DESC
            LIMIT :limit;
        """),
        {
            "user_id": user_id,
            "limit": limit
        }
    )

    redemptions = [
        {
            "redemption_id": row[0],
            "reward_id": row[1],
            "reward_title": row[2],
            "reward_description": row[3],
            "points_spent": row[4],
            "status_id": row[5],
            "status_code": row[6],
            "voucher_code": row[7],
            "requested_at": row[8],
            "fulfilled_at": row[9]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(redemptions),
        "redemptions": redemptions
    }


def list_admin_redemptions(
    db: Session,
    limit: int = 20,
    status_code: str | None = None
) -> dict:
    query = text("""
        SELECT
            r.redemption_id,
            u.user_id,
            u.email,
            r.reward_id,
            rw.title,
            rw.description,
            r.points_spent,
            rs.status_id,
            rs.status_code,
            r.voucher_code,
            r.requested_at,
            r.fulfilled_at
        FROM redemptions r
        INNER JOIN users u ON u.user_id = r.user_id
        INNER JOIN rewards rw ON rw.reward_id = r.reward_id
        INNER JOIN redemption_statuses rs ON rs.status_id = r.status_id
        WHERE (:status_code IS NULL OR rs.status_code = :status_code)
        ORDER BY r.requested_at DESC, r.redemption_id DESC
        LIMIT :limit;
    """)

    result = db.execute(
        query,
        {
            "status_code": status_code,
            "limit": limit
        }
    )
    redemptions = [_serialize_admin_redemption_row(row) for row in result.fetchall()]

    return {
        "count": len(redemptions),
        "redemptions": redemptions
    }


def get_admin_redemption_by_id(db: Session, redemption_id: int) -> dict | None:
    row = db.execute(
        text("""
            SELECT
                r.redemption_id,
                u.user_id,
                u.email,
                r.reward_id,
                rw.title,
                rw.description,
                r.points_spent,
                rs.status_id,
                rs.status_code,
                r.voucher_code,
                r.requested_at,
                r.fulfilled_at
            FROM redemptions r
            INNER JOIN users u ON u.user_id = r.user_id
            INNER JOIN rewards rw ON rw.reward_id = r.reward_id
            INNER JOIN redemption_statuses rs ON rs.status_id = r.status_id
            WHERE r.redemption_id = :redemption_id;
        """),
        {"redemption_id": redemption_id}
    ).fetchone()

    if row is None:
        return None

    return _serialize_admin_redemption_row(row)


def update_redemption_status_for_admin(
    db: Session,
    redemption_id: int,
    status_code: str,
    voucher_code: str | None = None
) -> dict:
    normalized_status_code = status_code.strip().upper()

    if normalized_status_code not in ADMIN_ALLOWED_STATUS_UPDATES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Status can only be updated to FULFILLED or CANCELLED"
        )

    existing_redemption = get_admin_redemption_by_id(db, redemption_id)

    if existing_redemption is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Redemption not found"
        )

    if existing_redemption["status_code"] != REQUESTED_REDEMPTION_STATUS:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Only redemptions in REQUESTED status can be updated"
        )

    next_status = _get_redemption_status(db, normalized_status_code)

    if normalized_status_code == FULFILLED_REDEMPTION_STATUS:
        db.execute(
            text("""
                UPDATE redemptions
                SET
                    status_id = :status_id,
                    voucher_code = :voucher_code,
                    fulfilled_at = NOW()
                WHERE redemption_id = :redemption_id;
            """),
            {
                "status_id": next_status["status_id"],
                "voucher_code": voucher_code.strip() if voucher_code and voucher_code.strip() else None,
                "redemption_id": redemption_id
            }
        )
        db.commit()

        updated_redemption = get_admin_redemption_by_id(db, redemption_id)

        return {
            "message": "Redemption fulfilled successfully",
            "redemption": updated_redemption,
            "refunded_points": 0
        }

    db.execute(
        text("""
            UPDATE redemptions
            SET
                status_id = :status_id,
                voucher_code = NULL,
                fulfilled_at = NULL
            WHERE redemption_id = :redemption_id;
        """),
        {
            "status_id": next_status["status_id"],
            "redemption_id": redemption_id
        }
    )
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
                NULL,
                'ADJUST',
                :points,
                NOW()
            );
        """),
        {
            "user_id": existing_redemption["user_id"],
            "points": existing_redemption["points_spent"]
        }
    )
    db.commit()

    updated_redemption = get_admin_redemption_by_id(db, redemption_id)

    return {
        "message": "Redemption cancelled and points refunded successfully",
        "redemption": updated_redemption,
        "refunded_points": existing_redemption["points_spent"]
    }


def _get_reward_for_redemption(db: Session, reward_id: int) -> dict:
    row = db.execute(
        text("""
            SELECT reward_id, title, description, points_cost, is_active
            FROM rewards
            WHERE reward_id = :reward_id;
        """),
        {"reward_id": reward_id}
    ).fetchone()

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Reward not found"
        )

    if not row[4]:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="This reward is inactive and cannot be redeemed"
        )

    return {
        "reward_id": row[0],
        "title": row[1],
        "description": row[2],
        "points_cost": row[3],
        "is_active": row[4]
    }


def _get_redemption_status(db: Session, status_code: str) -> dict:
    row = db.execute(
        text("""
            SELECT status_id, status_code
            FROM redemption_statuses
            WHERE status_code = :status_code;
        """),
        {"status_code": status_code}
    ).fetchone()

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Required redemption status is missing from the database"
        )

    return {
        "status_id": row[0],
        "status_code": row[1]
    }


def _serialize_admin_redemption_row(row) -> dict:
    return {
        "redemption_id": row[0],
        "user_id": row[1],
        "user_email": row[2],
        "reward_id": row[3],
        "reward_title": row[4],
        "reward_description": row[5],
        "points_spent": row[6],
        "status_id": row[7],
        "status_code": row[8],
        "voucher_code": row[9],
        "requested_at": row[10],
        "fulfilled_at": row[11]
    }
