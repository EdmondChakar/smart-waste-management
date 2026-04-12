from pydantic import BaseModel

from app.schemas.users import UserRead


class AdminSummaryCounts(BaseModel):
    total_users: int
    total_bins: int
    active_bins: int


class AdminSummaryResponse(BaseModel):
    message: str
    admin_user: UserRead
    summary: AdminSummaryCounts
