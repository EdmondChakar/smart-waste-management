from datetime import datetime

from pydantic import BaseModel


class DeviceCreate(BaseModel):
    bin_id: int
    device_uid: str
    is_active: bool = True


class DeviceRead(BaseModel):
    device_id: int
    bin_id: int
    device_uid: str
    last_seen_at: datetime | None
    is_active: bool


class DeviceCreateResponse(BaseModel):
    message: str
    device: DeviceRead
    api_key: str


class DeviceListResponse(BaseModel):
    count: int
    devices: list[DeviceRead]
