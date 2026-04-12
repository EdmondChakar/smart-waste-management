import secrets

from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.schemas.devices import DeviceCreate
from app.services.auth_service import hash_password


def list_devices(db: Session) -> dict:
    query = text("""
        SELECT device_id, bin_id, device_uid, last_seen_at, is_active
        FROM devices
        ORDER BY device_id;
    """)

    result = db.execute(query)
    devices = [
        {
            "device_id": row[0],
            "bin_id": row[1],
            "device_uid": row[2],
            "last_seen_at": row[3],
            "is_active": row[4]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(devices),
        "devices": devices
    }


def create_device_record(db: Session, device_data: DeviceCreate) -> dict:
    bin_query = text("""
        SELECT bin_id
        FROM bins
        WHERE bin_id = :bin_id;
    """)
    existing_bin = db.execute(bin_query, {"bin_id": device_data.bin_id}).fetchone()

    if existing_bin is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Bin not found for this device"
        )

    duplicate_query = text("""
        SELECT device_id
        FROM devices
        WHERE device_uid = :device_uid;
    """)
    existing_device = db.execute(
        duplicate_query,
        {"device_uid": device_data.device_uid}
    ).fetchone()

    if existing_device is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A device with this UID already exists"
        )

    api_key = secrets.token_urlsafe(24)
    api_key_hash = hash_password(api_key)

    insert_query = text("""
        INSERT INTO devices (bin_id, device_uid, api_key_hash, last_seen_at, is_active)
        VALUES (:bin_id, :device_uid, :api_key_hash, NULL, :is_active)
        RETURNING device_id, bin_id, device_uid, last_seen_at, is_active;
    """)

    result = db.execute(
        insert_query,
        {
            "bin_id": device_data.bin_id,
            "device_uid": device_data.device_uid,
            "api_key_hash": api_key_hash,
            "is_active": device_data.is_active
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "Device created successfully",
        "device": {
            "device_id": row[0],
            "bin_id": row[1],
            "device_uid": row[2],
            "last_seen_at": row[3],
            "is_active": row[4]
        },
        "api_key": api_key
    }


def get_device_by_uid(db: Session, device_uid: str) -> dict | None:
    query = text("""
        SELECT device_id, bin_id, device_uid, api_key_hash, last_seen_at, is_active
        FROM devices
        WHERE device_uid = :device_uid;
    """)

    result = db.execute(query, {"device_uid": device_uid})
    row = result.fetchone()

    if row is None:
        return None

    return {
        "device_id": row[0],
        "bin_id": row[1],
        "device_uid": row[2],
        "api_key_hash": row[3],
        "last_seen_at": row[4],
        "is_active": row[5]
    }
