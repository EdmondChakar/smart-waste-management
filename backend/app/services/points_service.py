from sqlalchemy import text
from sqlalchemy.orm import Session


def get_points_balance_summary(db: Session, user_id: int) -> dict:
    query = text("""
        SELECT
            COALESCE(SUM(
                CASE
                    WHEN type = 'REDEEM' THEN -ABS(points)
                    WHEN type = 'EARN' THEN ABS(points)
                    ELSE points
                END
            ), 0) AS current_points_balance,
            COALESCE(SUM(CASE WHEN type = 'EARN' THEN ABS(points) ELSE 0 END), 0) AS total_earned,
            COALESCE(SUM(CASE WHEN type = 'REDEEM' THEN ABS(points) ELSE 0 END), 0) AS total_redeemed,
            COALESCE(SUM(CASE WHEN type = 'ADJUST' THEN points ELSE 0 END), 0) AS total_adjusted,
            COUNT(*) AS total_transactions
        FROM points_txn
        WHERE user_id = :user_id;
    """)

    row = db.execute(query, {"user_id": user_id}).fetchone()

    return {
        "current_points_balance": int(row[0] or 0),
        "total_earned": int(row[1] or 0),
        "total_redeemed": int(row[2] or 0),
        "total_adjusted": int(row[3] or 0),
        "total_transactions": int(row[4] or 0)
    }


def get_points_history(db: Session, user_id: int, limit: int = 20) -> dict:
    query = text("""
        SELECT
            pt.txn_id,
            pt.type,
            pt.points,
            pt.created_at,
            pt.scan_id,
            se.bin_id,
            b.public_code,
            se.total_points_awarded
        FROM points_txn pt
        LEFT JOIN scan_event se ON se.scan_id = pt.scan_id
        LEFT JOIN bins b ON b.bin_id = se.bin_id
        WHERE pt.user_id = :user_id
        ORDER BY pt.created_at DESC, pt.txn_id DESC
        LIMIT :limit;
    """)

    result = db.execute(
        query,
        {
            "user_id": user_id,
            "limit": limit
        }
    )

    transactions = [
        {
            "txn_id": row[0],
            "type": row[1],
            "points": row[2],
            "created_at": row[3],
            "scan_id": row[4],
            "bin_id": row[5],
            "bin_code": row[6],
            "item_count": row[7]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(transactions),
        "transactions": transactions
    }
