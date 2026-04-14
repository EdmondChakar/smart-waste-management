from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.roles import ROLE_ADMIN
from app.schemas.admin import AdminSignupRequest
from app.services.auth_service import hash_password


def get_admin_summary(db: Session) -> dict:
    users_count_query = text("""
        SELECT COUNT(*)
        FROM users;
    """)
    bins_count_query = text("""
        SELECT COUNT(*)
        FROM bins;
    """)
    active_bins_query = text("""
        SELECT COUNT(*)
        FROM bins
        WHERE is_active = TRUE;
    """)

    total_users = db.execute(users_count_query).scalar() or 0
    total_bins = db.execute(bins_count_query).scalar() or 0
    active_bins = db.execute(active_bins_query).scalar() or 0

    return {
        "total_users": total_users,
        "total_bins": total_bins,
        "active_bins": active_bins
    }


def register_admin(db: Session, payload: AdminSignupRequest) -> dict:
    if not settings.ADMIN_SIGNUP_CODE:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Admin signup is not configured"
        )

    if payload.admin_code != settings.ADMIN_SIGNUP_CODE:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid admin code"
        )

    duplicate_query = text("""
        SELECT user_id
        FROM users
        WHERE LOWER(email) = LOWER(:email);
    """)
    existing_user = db.execute(
        duplicate_query,
        {"email": str(payload.email)}
    ).fetchone()

    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A user with this email already exists"
        )

    query = text("""
        INSERT INTO users (email, password_hash, role_id, status_id, created_at)
        VALUES (:email, :password_hash, :role_id, :status_id, NOW())
        RETURNING user_id, email, role_id, status_id, created_at;
    """)
    result = db.execute(
        query,
        {
            "email": payload.email,
            "password_hash": hash_password(payload.password),
            "role_id": ROLE_ADMIN,
            "status_id": 1
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "Admin account created successfully",
        "user": {
            "user_id": row[0],
            "email": row[1],
            "role_id": row[2],
            "status_id": row[3],
            "created_at": row[4]
        }
    }


def get_admin_bins_overview(db: Session) -> dict:
    query = text("""
        SELECT
            b.bin_id,
            b.public_code,
            b.is_active,
            bs.updated_at,
            bs.fill_pct,
            bs.weight_kg,
            bs.lat,
            bs.lon,
            bs.is_full,
            COUNT(d.device_id) AS device_count,
            MAX(d.last_seen_at) AS last_seen_at
        FROM bins b
        LEFT JOIN bin_status bs
            ON bs.bin_id = b.bin_id
        LEFT JOIN devices d
            ON d.bin_id = b.bin_id
        GROUP BY
            b.bin_id,
            b.public_code,
            b.is_active,
            bs.updated_at,
            bs.fill_pct,
            bs.weight_kg,
            bs.lat,
            bs.lon,
            bs.is_full
        ORDER BY b.bin_id;
    """)

    result = db.execute(query)
    bins = [
        {
            "bin_id": row[0],
            "public_code": row[1],
            "is_active": row[2],
            "updated_at": row[3],
            "fill_pct": float(row[4]) if row[4] is not None else None,
            "weight_kg": float(row[5]) if row[5] is not None else None,
            "lat": float(row[6]) if row[6] is not None else None,
            "lon": float(row[7]) if row[7] is not None else None,
            "is_full": row[8],
            "device_count": row[9],
            "last_seen_at": row[10]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(bins),
        "bins": bins
    }


def get_admin_bin_detail(db: Session, bin_id: int) -> dict | None:
    query = text("""
        SELECT
            b.bin_id,
            b.public_code,
            b.is_active,
            bs.updated_at,
            bs.fill_pct,
            bs.weight_kg,
            bs.lat,
            bs.lon,
            bs.is_full,
            bs.last_reading_id,
            COUNT(d.device_id) AS device_count,
            MAX(d.last_seen_at) AS last_seen_at
        FROM bins b
        LEFT JOIN bin_status bs
            ON bs.bin_id = b.bin_id
        LEFT JOIN devices d
            ON d.bin_id = b.bin_id
        WHERE b.bin_id = :bin_id
        GROUP BY
            b.bin_id,
            b.public_code,
            b.is_active,
            bs.updated_at,
            bs.fill_pct,
            bs.weight_kg,
            bs.lat,
            bs.lon,
            bs.is_full,
            bs.last_reading_id;
    """)

    result = db.execute(query, {"bin_id": bin_id})
    row = result.fetchone()

    if row is None:
        return None

    return {
        "bin_id": row[0],
        "public_code": row[1],
        "is_active": row[2],
        "updated_at": row[3],
        "fill_pct": float(row[4]) if row[4] is not None else None,
        "weight_kg": float(row[5]) if row[5] is not None else None,
        "lat": float(row[6]) if row[6] is not None else None,
        "lon": float(row[7]) if row[7] is not None else None,
        "is_full": row[8],
        "last_reading_id": row[9],
        "device_count": row[10],
        "last_seen_at": row[11]
    }


def get_recent_bin_readings(db: Session, bin_id: int, limit: int = 20) -> dict:
    query = text("""
        SELECT
            reading_id,
            bin_id,
            device_id,
            captured_at,
            fill_pct,
            weight_kg,
            gps_lat,
            gps_lon
        FROM sensor_reading
        WHERE bin_id = :bin_id
        ORDER BY captured_at DESC
        LIMIT :limit_value;
    """)

    result = db.execute(
        query,
        {
            "bin_id": bin_id,
            "limit_value": limit
        }
    )
    readings = [
        {
            "reading_id": row[0],
            "bin_id": row[1],
            "device_id": row[2],
            "captured_at": row[3],
            "fill_pct": float(row[4]) if row[4] is not None else None,
            "weight_kg": float(row[5]) if row[5] is not None else None,
            "gps_lat": float(row[6]) if row[6] is not None else None,
            "gps_lon": float(row[7]) if row[7] is not None else None
        }
        for row in result.fetchall()
    ]

    return {
        "bin_id": bin_id,
        "count": len(readings),
        "readings": readings
    }
