from fastapi import APIRouter, Depends
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.db.session import get_db

router = APIRouter()


@router.get("/roles")
def get_roles(db: Session = Depends(get_db)):
    query = text("""
        SELECT *
        FROM roles
        ORDER BY 1;
    """)

    result = db.execute(query)
    rows = result.fetchall()
    columns = list(result.keys())

    roles = [dict(zip(columns, row)) for row in rows]

    return {
        "count": len(roles),
        "roles": roles
    }


@router.get("/user-statuses")
def get_user_statuses(db: Session = Depends(get_db)):
    query = text("""
        SELECT *
        FROM user_statuses
        ORDER BY 1;
    """)

    result = db.execute(query)
    rows = result.fetchall()
    columns = list(result.keys())

    statuses = [dict(zip(columns, row)) for row in rows]

    return {
        "count": len(statuses),
        "user_statuses": statuses
    }
