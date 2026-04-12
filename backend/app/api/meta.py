from fastapi import APIRouter, Depends
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.db.session import get_db

router = APIRouter()


@router.get("/tables")
def get_tables(db: Session = Depends(get_db)):
    query = text("""
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
        ORDER BY table_name;
    """)

    result = db.execute(query)
    tables = [row[0] for row in result.fetchall()]

    return {
        "database": "smart_waste",
        "tables": tables
    }


@router.get("/table-columns/{table_name}")
def get_table_columns(table_name: str, db: Session = Depends(get_db)):
    query = text("""
        SELECT
            column_name,
            data_type,
            is_nullable
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = :table_name
        ORDER BY ordinal_position;
    """)

    result = db.execute(query, {"table_name": table_name})
    columns = [
        {
            "column_name": row[0],
            "data_type": row[1],
            "is_nullable": row[2]
        }
        for row in result.fetchall()
    ]

    return {
        "table": table_name,
        "columns": columns
    }
