from datetime import datetime

from pydantic import BaseModel, Field


class RedemptionRead(BaseModel):
    redemption_id: int
    reward_id: int
    reward_title: str
    reward_description: str | None = None
    points_spent: int
    status_id: int
    status_code: str
    voucher_code: str | None = None
    requested_at: datetime
    fulfilled_at: datetime | None = None


class RedemptionRedeemResponse(BaseModel):
    message: str
    redemption: RedemptionRead
    current_points_balance: int


class RedemptionHistoryResponse(BaseModel):
    count: int
    redemptions: list[RedemptionRead]


class AdminRedemptionRead(BaseModel):
    redemption_id: int
    user_id: int
    user_email: str
    reward_id: int
    reward_title: str
    reward_description: str | None = None
    points_spent: int
    status_id: int
    status_code: str
    voucher_code: str | None = None
    requested_at: datetime
    fulfilled_at: datetime | None = None


class AdminRedemptionListResponse(BaseModel):
    count: int
    redemptions: list[AdminRedemptionRead]


class AdminRedemptionStatusUpdateRequest(BaseModel):
    status_code: str = Field(min_length=1, max_length=20)
    voucher_code: str | None = Field(default=None, max_length=255)


class AdminRedemptionStatusUpdateResponse(BaseModel):
    message: str
    redemption: AdminRedemptionRead
    refunded_points: int
