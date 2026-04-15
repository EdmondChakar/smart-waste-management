from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN, ROLE_USER
from app.db.session import get_db
from app.schemas.redemptions import (
    AdminRedemptionListResponse,
    AdminRedemptionRead,
    AdminRedemptionStatusUpdateRequest,
    AdminRedemptionStatusUpdateResponse,
    RedemptionHistoryResponse,
    RedemptionRedeemResponse,
)
from app.services.redemptions_service import (
    get_admin_redemption_by_id,
    get_redemption_history,
    list_admin_redemptions,
    redeem_reward_for_user,
    update_redemption_status_for_admin,
)

router = APIRouter()


@router.post("/rewards/{reward_id}/redeem", response_model=RedemptionRedeemResponse)
def redeem_reward(
    reward_id: int,
    current_user: dict = Depends(require_roles({ROLE_USER})),
    db: Session = Depends(get_db)
):
    return redeem_reward_for_user(
        db=db,
        user_id=current_user["user_id"],
        reward_id=reward_id
    )


@router.get("/redemptions/me", response_model=RedemptionHistoryResponse)
def read_my_redemptions(
    limit: int = Query(default=20, ge=1, le=100),
    current_user: dict = Depends(require_roles({ROLE_USER})),
    db: Session = Depends(get_db)
):
    return get_redemption_history(
        db=db,
        user_id=current_user["user_id"],
        limit=limit
    )


@router.get("/admin/redemptions", response_model=AdminRedemptionListResponse)
def read_admin_redemptions(
    limit: int = Query(default=20, ge=1, le=100),
    status_code: str | None = Query(default=None),
    current_user: dict = Depends(require_roles({ROLE_ADMIN})),
    db: Session = Depends(get_db)
):
    normalized_status_code = status_code.strip().upper() if status_code else None

    return list_admin_redemptions(
        db=db,
        limit=limit,
        status_code=normalized_status_code
    )


@router.get("/admin/redemptions/{redemption_id}", response_model=AdminRedemptionRead)
def read_admin_redemption(
    redemption_id: int,
    current_user: dict = Depends(require_roles({ROLE_ADMIN})),
    db: Session = Depends(get_db)
):
    redemption = get_admin_redemption_by_id(db, redemption_id)

    if redemption is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Redemption not found"
        )

    return redemption


@router.put(
    "/admin/redemptions/{redemption_id}/status",
    response_model=AdminRedemptionStatusUpdateResponse
)
def update_admin_redemption_status(
    redemption_id: int,
    payload: AdminRedemptionStatusUpdateRequest,
    current_user: dict = Depends(require_roles({ROLE_ADMIN})),
    db: Session = Depends(get_db)
):
    return update_redemption_status_for_admin(
        db=db,
        redemption_id=redemption_id,
        status_code=payload.status_code,
        voucher_code=payload.voucher_code
    )
