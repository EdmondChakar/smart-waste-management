from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN
from app.db.session import get_db
from app.schemas.rewards import (
    RewardCreate,
    RewardListResponse,
    RewardRead,
    RewardUpdate,
    RewardWriteResponse,
)
from app.services.rewards_service import (
    create_reward_record,
    get_reward_by_id,
    list_rewards,
    update_reward_record,
)

router = APIRouter()


@router.get("/rewards", response_model=RewardListResponse)
def get_active_rewards(db: Session = Depends(get_db)):
    return list_rewards(db, active_only=True)


@router.get("/admin/rewards", response_model=RewardListResponse)
def get_admin_rewards(
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return list_rewards(db, active_only=False)


@router.get("/admin/rewards/{reward_id}", response_model=RewardRead)
def get_admin_reward(
    reward_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    reward = get_reward_by_id(db, reward_id)

    if reward is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Reward not found"
        )

    return reward


@router.post("/admin/rewards", response_model=RewardWriteResponse)
def create_admin_reward(
    reward_data: RewardCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return create_reward_record(db, reward_data)


@router.put("/admin/rewards/{reward_id}", response_model=RewardWriteResponse)
def update_admin_reward(
    reward_id: int,
    reward_data: RewardUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN}))
):
    return update_reward_record(db, reward_id, reward_data)
