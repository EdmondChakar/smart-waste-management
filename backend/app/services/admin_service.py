from sqlalchemy import text
from sqlalchemy.orm import Session


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
