from datetime import datetime

from pydantic import BaseModel


class BinCreate(BaseModel):
    public_code: str
    is_active: bool = True


class BinRead(BaseModel):
    bin_id: int
    public_code: str
    is_active: bool
    created_at: datetime


class BinListResponse(BaseModel):
    count: int
    bins: list[BinRead]


class BinCreateResponse(BaseModel):
    message: str
    bin: BinRead


class BinStatusRead(BaseModel):
    bin_id: int
    updated_at: datetime
    fill_pct: float | None
    weight_kg: float | None
    lat: float | None
    lon: float | None
    is_full: bool
    last_reading_id: int | None
