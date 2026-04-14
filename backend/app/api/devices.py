from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN
from app.db.session import get_db
from app.schemas.devices import (
    DeviceApiKeyResetResponse,
    DeviceCreate,
    DeviceCreateResponse,
    DeviceListResponse,
)
from app.services.devices_service import (
    create_device_record,
    list_devices,
    regenerate_device_api_key,
)

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


@router.post("/devices/{device_id}/regenerate-api-key", response_model=DeviceApiKeyResetResponse)
def regenerate_api_key_for_device(
    device_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return regenerate_device_api_key(db, device_id)
