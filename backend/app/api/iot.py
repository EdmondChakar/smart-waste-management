from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.sensor import SensorReadingIngestRequest, SensorReadingIngestResponse
from app.services.sensor_service import ingest_sensor_reading

router = APIRouter()


@router.post("/iot/readings", response_model=SensorReadingIngestResponse)
def create_sensor_reading(
    payload: SensorReadingIngestRequest,
    db: Session = Depends(get_db)
):
    return ingest_sensor_reading(db, payload)
