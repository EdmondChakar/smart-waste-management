from datetime import datetime

from pydantic import BaseModel, EmailStr


class UserCreate(BaseModel):
    email: EmailStr
    password: str
    role_id: int = 1
    status_id: int = 1


class UserRead(BaseModel):
    user_id: int
    email: EmailStr
    role_id: int
    status_id: int
    created_at: datetime


class UserListResponse(BaseModel):
    count: int
    users: list[UserRead]


class UserCreateResponse(BaseModel):
    message: str
    user: UserRead
