from datetime import datetime

from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.config import settings
from app.schemas.sensor import SensorReadingIngestRequest
from app.services.auth_service import verify_password
from app.services.devices_service import get_device_by_uid


def ingest_sensor_reading(db: Session, payload: SensorReadingIngestRequest) -> dict:
    device = get_device_by_uid(db, payload.device_uid)

    if device is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Device not found"
        )

    if not device["is_active"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Device is inactive"
        )

    if not verify_password(payload.api_key, device["api_key_hash"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid device credentials"
        )

    captured_at = payload.captured_at or datetime.utcnow()
    is_full = payload.fill_pct >= settings.BIN_FULL_THRESHOLD_PCT

    insert_reading_query = text("""
        INSERT INTO sensor_reading (
            bin_id,
            device_id,
            captured_at,
            fill_pct,
            weight_kg,
            gps_lat,
            gps_lon
        )
        VALUES (
            :bin_id,
            :device_id,
            :captured_at,
            :fill_pct,
            :weight_kg,
            :gps_lat,
            :gps_lon
        )
        RETURNING reading_id, bin_id, device_id, captured_at, fill_pct, weight_kg, gps_lat, gps_lon;
    """)

    reading_result = db.execute(
        insert_reading_query,
        {
            "bin_id": device["bin_id"],
            "device_id": device["device_id"],
            "captured_at": captured_at,
            "fill_pct": payload.fill_pct,
            "weight_kg": payload.weight_kg,
            "gps_lat": payload.gps_lat,
            "gps_lon": payload.gps_lon
        }
    )
    reading_row = reading_result.fetchone()

    upsert_status_query = text("""
        INSERT INTO bin_status (
            bin_id,
            updated_at,
            fill_pct,
            weight_kg,
            lat,
            lon,
            is_full,
            last_reading_id
        )
        VALUES (
            :bin_id,
            :updated_at,
            :fill_pct,
            :weight_kg,
            :lat,
            :lon,
            :is_full,
            :last_reading_id
        )
        ON CONFLICT (bin_id)
        DO UPDATE SET
            updated_at = EXCLUDED.updated_at,
            fill_pct = EXCLUDED.fill_pct,
            weight_kg = EXCLUDED.weight_kg,
            lat = COALESCE(EXCLUDED.lat, bin_status.lat),
            lon = COALESCE(EXCLUDED.lon, bin_status.lon),
            is_full = EXCLUDED.is_full,
            last_reading_id = EXCLUDED.last_reading_id
        RETURNING bin_id, updated_at, fill_pct, weight_kg, lat, lon, is_full, last_reading_id;
    """)

    status_result = db.execute(
        upsert_status_query,
        {
            "bin_id": device["bin_id"],
            "updated_at": captured_at,
            "fill_pct": payload.fill_pct,
            "weight_kg": payload.weight_kg,
            "lat": payload.gps_lat,
            "lon": payload.gps_lon,
            "is_full": is_full,
            "last_reading_id": reading_row[0]
        }
    )
    status_row = status_result.fetchone()

    update_device_query = text("""
        UPDATE devices
        SET last_seen_at = :last_seen_at
        WHERE device_id = :device_id;
    """)
    db.execute(
        update_device_query,
        {
            "last_seen_at": captured_at,
            "device_id": device["device_id"]
        }
    )
    db.commit()

    return {
        "message": "Sensor reading ingested successfully",
        "reading": {
            "reading_id": reading_row[0],
            "bin_id": reading_row[1],
            "device_id": reading_row[2],
            "captured_at": reading_row[3],
            "fill_pct": float(reading_row[4]) if reading_row[4] is not None else None,
            "weight_kg": float(reading_row[5]) if reading_row[5] is not None else None,
            "gps_lat": float(reading_row[6]) if reading_row[6] is not None else None,
            "gps_lon": float(reading_row[7]) if reading_row[7] is not None else None
        },
        "bin_status": {
            "bin_id": status_row[0],
            "updated_at": status_row[1],
            "fill_pct": float(status_row[2]) if status_row[2] is not None else None,
            "weight_kg": float(status_row[3]) if status_row[3] is not None else None,
            "lat": float(status_row[4]) if status_row[4] is not None else None,
            "lon": float(status_row[5]) if status_row[5] is not None else None,
            "is_full": status_row[6],
            "last_reading_id": status_row[7]
        }
    }
