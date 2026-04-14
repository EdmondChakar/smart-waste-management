from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.db.session import get_db
from app.schemas.points import PointsBalanceRead, PointsHistoryResponse
from app.services.points_service import get_points_balance_summary, get_points_history

router = APIRouter()


@router.get("/points/balance", response_model=PointsBalanceRead)
def read_points_balance(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return get_points_balance_summary(db, current_user["user_id"])


@router.get("/points/history", response_model=PointsHistoryResponse)
def read_points_history(
    limit: int = Query(default=20, ge=1, le=100),
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return get_points_history(
        db=db,
        user_id=current_user["user_id"],
        limit=limit
    )
