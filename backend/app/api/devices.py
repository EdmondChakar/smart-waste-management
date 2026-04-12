from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN
from app.db.session import get_db
from app.schemas.devices import DeviceCreate, DeviceCreateResponse, DeviceListResponse
from app.services.devices_service import create_device_record, list_devices

router = APIRouter()


@router.get("/devices", response_model=DeviceListResponse)
def get_devices(
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return list_devices(db)


@router.post("/devices", response_model=DeviceCreateResponse)
def create_device(
    device_data: DeviceCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return create_device_record(db, device_data)
