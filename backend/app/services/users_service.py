from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.schemas.users import UserCreate
from app.services.auth_service import hash_password


def get_all_users(db: Session) -> dict:
    query = text("""
        SELECT user_id, email, role_id, status_id, created_at
        FROM users
        ORDER BY user_id;
    """)

    result = db.execute(query)
    users = [
        {
            "user_id": row[0],
            "email": row[1],
            "role_id": row[2],
            "status_id": row[3],
            "created_at": row[4]
        }
        for row in result.fetchall()
    ]

    return {
        "count": len(users),
        "users": users
    }


def create_user_record(db: Session, user_data: UserCreate) -> dict:
    duplicate_query = text("""
        SELECT user_id
        FROM users
        WHERE LOWER(email) = LOWER(:email);
    """)

    existing_user = db.execute(
        duplicate_query,
        {"email": str(user_data.email)}
    ).fetchone()

    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A user with this email already exists"
        )

    hashed_password = hash_password(user_data.password)

    query = text("""
        INSERT INTO users (email, password_hash, role_id, status_id, created_at)
        VALUES (:email, :password_hash, :role_id, :status_id, NOW())
        RETURNING user_id, email, role_id, status_id, created_at;
    """)

    result = db.execute(
        query,
        {
            "email": user_data.email,
            "password_hash": hashed_password,
            "role_id": user_data.role_id,
            "status_id": user_data.status_id
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "User created successfully",
        "user": {
            "user_id": row[0],
            "email": row[1],
            "role_id": row[2],
            "status_id": row[3],
            "created_at": row[4]
        }
    }
