from datetime import datetime

from pydantic import BaseModel, Field


class ScanClaimRequest(BaseModel):
    qr_raw: str = Field(min_length=1)


class ScanClaimRead(BaseModel):
    scan_id: int
    bin_id: int
    bin_code: str
    item_count: int
    points_awarded: int
    scan_at: datetime
    is_valid: bool
    invalid_reason: str | None = None


class ScanClaimResponse(BaseModel):
    message: str
    scan: ScanClaimRead
    current_points_balance: int
