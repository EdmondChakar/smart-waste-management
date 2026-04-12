from datetime import datetime, timedelta, timezone

import jwt
from fastapi import HTTPException, status
from pwdlib import PasswordHash
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.config import settings


password_hasher = PasswordHash.recommended()


def hash_password(password: str) -> str:
    return password_hasher.hash(password)


def verify_password(password: str, hashed_password: str) -> bool:
    return password_hasher.verify(password, hashed_password)


def _build_login_user(row) -> dict:
    return {
        "user_id": row[0],
        "email": row[1],
        "password_hash": row[2],
        "role_id": row[3],
        "status_id": row[4],
        "created_at": row[5]
    }


def _public_user(user: dict) -> dict:
    return {
        "user_id": user["user_id"],
        "email": user["email"],
        "role_id": user["role_id"],
        "status_id": user["status_id"],
        "created_at": user["created_at"]
    }


def create_access_token(user: dict) -> str:
    expires_at = datetime.now(timezone.utc) + timedelta(
        minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES
    )
    payload = {
        "sub": str(user["user_id"]),
        "email": user["email"],
        "role_id": user["role_id"],
        "exp": expires_at
    }
    return jwt.encode(
        payload,
        settings.JWT_SECRET_KEY,
        algorithm=settings.JWT_ALGORITHM
    )


def decode_access_token(token: str) -> dict:
    try:
        return jwt.decode(
            token,
            settings.JWT_SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM]
        )
    except jwt.InvalidTokenError as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token"
        ) from exc


def get_user_by_id(db: Session, user_id: int) -> dict | None:
    query = text("""
        SELECT user_id, email, role_id, status_id, created_at
        FROM users
        WHERE user_id = :user_id;
    """)

    result = db.execute(query, {"user_id": user_id})
    row = result.fetchone()

    if row is None:
        return None

    return {
        "user_id": row[0],
        "email": row[1],
        "role_id": row[2],
        "status_id": row[3],
        "created_at": row[4]
    }


def authenticate_user(db: Session, email: str, password: str) -> dict:
    query = text("""
        SELECT user_id, email, password_hash, role_id, status_id, created_at
        FROM users
        WHERE email = :email;
    """)

    result = db.execute(query, {"email": email})
    row = result.fetchone()

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    user = _build_login_user(row)

    if not verify_password(password, user["password_hash"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    access_token = create_access_token(user)

    return {
        "message": "Login successful",
        "access_token": access_token,
        "token_type": "bearer",
        "user": _public_user(user)
    }
