from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.schemas.bins import BinCreate


def get_all_bins(db: Session) -> dict:
    query = text("""
        SELECT bin_id, public_code, is_active, created_at
        FROM bins
        ORDER BY bin_id;
    """)

    result = db.execute(query)
    bins = [
        {
            "bin_id": row[0],
            "public_code": row[1],
            "is_active": row[2],
            "created_at": row[3]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(bins),
        "bins": bins
    }


def get_bin_status(db: Session, bin_id: int) -> dict | None:
    query = text("""
        SELECT bin_id, updated_at, fill_pct, weight_kg, lat, lon, is_full, last_reading_id
        FROM bin_status
        WHERE bin_id = :bin_id;
    """)

    result = db.execute(query, {"bin_id": bin_id})
    row = result.fetchone()

    if row is None:
        return None

    return {
        "bin_id": row[0],
        "updated_at": row[1],
        "fill_pct": float(row[2]) if row[2] is not None else None,
        "weight_kg": float(row[3]) if row[3] is not None else None,
        "lat": float(row[4]) if row[4] is not None else None,
        "lon": float(row[5]) if row[5] is not None else None,
        "is_full": row[6],
        "last_reading_id": row[7]
    }


def create_bin_record(db: Session, bin_data: BinCreate) -> dict:
    duplicate_query = text("""
        SELECT bin_id
        FROM bins
        WHERE public_code = :public_code;
    """)

    existing_bin = db.execute(
        duplicate_query,
        {"public_code": bin_data.public_code}
    ).fetchone()

    if existing_bin is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A bin with this public code already exists"
        )

    query = text("""
        INSERT INTO bins (public_code, is_active, created_at)
        VALUES (:public_code, :is_active, NOW())
        RETURNING bin_id, public_code, is_active, created_at;
    """)

    result = db.execute(
        query,
        {
            "public_code": bin_data.public_code,
            "is_active": bin_data.is_active
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "Bin created successfully",
        "bin": {
            "bin_id": row[0],
            "public_code": row[1],
            "is_active": row[2],
            "created_at": row[3]
        }
    }
