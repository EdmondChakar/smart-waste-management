from fastapi import APIRouter

from app.api.admin import router as admin_router
from app.api.auth import router as auth_router
from app.api.bins import router as bins_router
from app.api.devices import router as devices_router
from app.api.iot import router as iot_router
from app.api.lookups import router as lookups_router
from app.api.meta import router as meta_router
from app.api.system import router as system_router
from app.api.users import router as users_router

api_router = APIRouter()

api_router.include_router(system_router)
api_router.include_router(meta_router)
api_router.include_router(auth_router)
api_router.include_router(admin_router)
api_router.include_router(users_router)
api_router.include_router(bins_router)
api_router.include_router(devices_router)
api_router.include_router(iot_router)
api_router.include_router(lookups_router)
