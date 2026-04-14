from datetime import datetime

from pydantic import BaseModel


class PointsBalanceRead(BaseModel):
    current_points_balance: int
    total_earned: int
    total_redeemed: int
    total_adjusted: int
    total_transactions: int


class PointsHistoryItemRead(BaseModel):
    txn_id: int
    type: str
    points: int
    created_at: datetime
    scan_id: int | None = None
    bin_id: int | None = None
    bin_code: str | None = None
    item_count: int | None = None


class PointsHistoryResponse(BaseModel):
    count: int
    transactions: list[PointsHistoryItemRead]
