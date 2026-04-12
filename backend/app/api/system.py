from fastapi import APIRouter
from sqlalchemy import text

from app.db.session import engine

router = APIRouter()


@router.get("/")
def root():
    return {"message": "Smart Waste Management backend is running"}


@router.get("/health")
def health_check():
    return {"status": "ok"}


@router.get("/db-test")
def db_test():
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT 1"))
            value = result.scalar()

        return {
            "database_connection": "successful",
            "test_value": value
        }
    except Exception as e:
        return {
            "database_connection": "failed",
            "error": str(e)
        }
