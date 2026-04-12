from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN
from app.db.session import get_db
from app.schemas.admin import AdminSummaryResponse
from app.services.admin_service import get_admin_summary

router = APIRouter()


@router.get("/admin/summary", response_model=AdminSummaryResponse)
def read_admin_summary(
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    summary = get_admin_summary(db)

    return {
        "message": "Admin summary fetched successfully",
        "admin_user": current_user,
        "summary": summary
    }
