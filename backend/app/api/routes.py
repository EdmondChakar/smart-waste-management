from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, EmailStr
from pwdlib import PasswordHash
from sqlalchemy import text
from sqlalchemy.orm import Session
from app.db.session import engine, get_db

router = APIRouter()

password_hasher = PasswordHash.recommended()


class BinCreate(BaseModel):
    public_code: str
    is_active: bool = True


class UserCreate(BaseModel):
    email: EmailStr
    password: str
    role_id: int = 1
    status_id: int = 1


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


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


@router.get("/bins")
def get_bins(db: Session = Depends(get_db)):
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


@router.post("/bins")
def create_bin(bin_data: BinCreate, db: Session = Depends(get_db)):
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


@router.get("/users")
def get_users(db: Session = Depends(get_db)):
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


@router.post("/users")
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    hashed_password = password_hasher.hash(user_data.password)

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


@router.post("/login")
def login(login_data: LoginRequest, db: Session = Depends(get_db)):
    query = text("""
        SELECT user_id, email, password_hash, role_id, status_id, created_at
        FROM users
        WHERE email = :email;
    """)

    result = db.execute(query, {"email": login_data.email})
    row = result.fetchone()

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    password_is_valid = password_hasher.verify(
        login_data.password,
        row[2]
    )

    if not password_is_valid:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    return {
        "message": "Login successful",
        "user": {
            "user_id": row[0],
            "email": row[1],
            "role_id": row[3],
            "status_id": row[4],
            "created_at": row[5]
        }
    }


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