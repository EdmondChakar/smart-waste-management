from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.users import UserCreate, UserCreateResponse, UserListResponse
from app.services.users_service import create_user_record, get_all_users

router = APIRouter()


@router.get("/users", response_model=UserListResponse)
def get_users(db: Session = Depends(get_db)):
    return get_all_users(db)


@router.post("/users", response_model=UserCreateResponse)
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    return create_user_record(db, user_data)
