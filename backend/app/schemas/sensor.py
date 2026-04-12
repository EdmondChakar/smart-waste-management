from datetime import datetime

from pydantic import BaseModel, Field, model_validator

from app.schemas.bins import BinStatusRead


class SensorReadingIngestRequest(BaseModel):
    device_uid: str
    api_key: str
    fill_pct: float = Field(ge=0, le=100)
    weight_kg: float = Field(ge=0)
    gps_lat: float | None = Field(default=None, ge=-90, le=90)
    gps_lon: float | None = Field(default=None, ge=-180, le=180)
    captured_at: datetime | None = None

    @model_validator(mode="after")
    def validate_gps_pair(self):
        if (self.gps_lat is None) != (self.gps_lon is None):
            raise ValueError("gps_lat and gps_lon must be provided together")
        return self


class SensorReadingRead(BaseModel):
    reading_id: int
    bin_id: int
    device_id: int
    captured_at: datetime
    fill_pct: float | None
    weight_kg: float | None
    gps_lat: float | None
    gps_lon: float | None


class SensorReadingIngestResponse(BaseModel):
    message: str
    reading: SensorReadingRead
    bin_status: BinStatusRead
