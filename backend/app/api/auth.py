from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.db.session import get_db
from app.schemas.auth import LoginRequest, LoginResponse
from app.schemas.users import UserRead
from app.services.auth_service import authenticate_user

router = APIRouter()


@router.post("/login", response_model=LoginResponse)
def login(login_data: LoginRequest, db: Session = Depends(get_db)):
    return authenticate_user(
        db=db,
        email=login_data.email,
        password=login_data.password
    )


@router.get("/me", response_model=UserRead)
def read_current_user(current_user: dict = Depends(get_current_user)):
    return current_user
