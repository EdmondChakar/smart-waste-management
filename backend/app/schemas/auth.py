from pydantic import BaseModel, EmailStr

from app.schemas.users import UserRead


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class LoginResponse(BaseModel):
    message: str
    access_token: str
    token_type: str
    user: UserRead
