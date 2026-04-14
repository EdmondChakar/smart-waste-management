from pydantic import BaseModel, Field


class RewardCreate(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    description: str | None = None
    points_cost: int = Field(ge=0)
    is_active: bool = True


class RewardUpdate(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    description: str | None = None
    points_cost: int = Field(ge=0)
    is_active: bool


class RewardRead(BaseModel):
    reward_id: int
    title: str
    description: str | None
    points_cost: int
    is_active: bool


class RewardListResponse(BaseModel):
    count: int
    rewards: list[RewardRead]


class RewardWriteResponse(BaseModel):
    message: str
    reward: RewardRead
