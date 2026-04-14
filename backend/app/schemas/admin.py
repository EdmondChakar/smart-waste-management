from datetime import datetime

from pydantic import BaseModel, EmailStr

from app.schemas.users import UserRead


class AdminSummaryCounts(BaseModel):
    total_users: int
    total_bins: int
    active_bins: int


class AdminSummaryResponse(BaseModel):
    message: str
    admin_user: UserRead
    summary: AdminSummaryCounts


class AdminSignupRequest(BaseModel):
    full_name: str
    email: EmailStr
    password: str
    admin_code: str


class AdminSignupResponse(BaseModel):
    message: str
    user: UserRead


class AdminBinOverviewItem(BaseModel):
    bin_id: int
    public_code: str
    is_active: bool
    updated_at: datetime | None
    fill_pct: float | None
    weight_kg: float | None
    lat: float | None
    lon: float | None
    is_full: bool | None
    device_count: int
    last_seen_at: datetime | None


class AdminBinOverviewResponse(BaseModel):
    count: int
    bins: list[AdminBinOverviewItem]


class AdminBinDetailResponse(BaseModel):
    bin_id: int
    public_code: str
    is_active: bool
    updated_at: datetime | None
    fill_pct: float | None
    weight_kg: float | None
    lat: float | None
    lon: float | None
    is_full: bool | None
    last_reading_id: int | None
    device_count: int
    last_seen_at: datetime | None


class AdminRecentReadingItem(BaseModel):
    reading_id: int
    bin_id: int
    device_id: int
    captured_at: datetime
    fill_pct: float | None
    weight_kg: float | None
    gps_lat: float | None
    gps_lon: float | None


class AdminRecentReadingsResponse(BaseModel):
    bin_id: int
    count: int
    readings: list[AdminRecentReadingItem]
