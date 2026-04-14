from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN
from app.db.session import get_db
from app.schemas.admin import (
    AdminBinDetailResponse,
    AdminBinOverviewResponse,
    AdminRecentReadingsResponse,
    AdminSignupRequest,
    AdminSignupResponse,
    AdminSummaryResponse,
)
from app.schemas.auth import LoginRequest, LoginResponse
from app.services.admin_service import (
    get_admin_bins_overview,
    get_admin_bin_detail,
    get_admin_summary,
    get_recent_bin_readings,
    register_admin,
)
from app.services.auth_service import authenticate_admin

router = APIRouter()


@router.post("/admin/signup", response_model=AdminSignupResponse)
def admin_signup(payload: AdminSignupRequest, db: Session = Depends(get_db)):
    return register_admin(db, payload)


@router.post("/admin/login", response_model=LoginResponse)
def admin_login(login_data: LoginRequest, db: Session = Depends(get_db)):
    return authenticate_admin(
        db=db,
        email=login_data.email,
        password=login_data.password
    )


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


@router.get("/admin/bins/overview", response_model=AdminBinOverviewResponse)
def read_admin_bins_overview(
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return get_admin_bins_overview(db)


@router.get("/admin/bins/{bin_id}", response_model=AdminBinDetailResponse)
def read_admin_bin_detail(
    bin_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    bin_detail = get_admin_bin_detail(db, bin_id)

    if bin_detail is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Bin not found"
        )

    return bin_detail


@router.get("/admin/bins/{bin_id}/readings", response_model=AdminRecentReadingsResponse)
def read_admin_bin_readings(
    bin_id: int,
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    if get_admin_bin_detail(db, bin_id) is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Bin not found"
        )

    return get_recent_bin_readings(db, bin_id, limit)
