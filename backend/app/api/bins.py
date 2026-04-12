from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import require_roles
from app.core.roles import ROLE_ADMIN, ROLE_COLLECTOR
from app.db.session import get_db
from app.schemas.bins import BinCreate, BinCreateResponse, BinListResponse, BinStatusRead
from app.services.bins_service import create_bin_record, get_all_bins, get_bin_status

router = APIRouter()

@router.get("/bins", response_model=BinListResponse)
def get_bins(db: Session = Depends(get_db)):
    return get_all_bins(db)


@router.post("/bins", response_model=BinCreateResponse)
def create_bin(bin_data: BinCreate, db: Session = Depends(get_db)):
    return create_bin_record(db, bin_data)


@router.get("/bins/{bin_id}/status", response_model=BinStatusRead)
def read_bin_status(
    bin_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_roles({ROLE_ADMIN, ROLE_COLLECTOR}))
):
    bin_status = get_bin_status(db, bin_id)

    if bin_status is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No current status found for this bin"
        )

    return bin_status
