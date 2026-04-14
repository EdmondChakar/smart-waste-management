from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.db.session import get_db
from app.schemas.scan import ScanClaimRequest, ScanClaimResponse
from app.services.scan_service import claim_scan_from_qr

router = APIRouter()


@router.post("/scan/claim", response_model=ScanClaimResponse)
def claim_scan(
    payload: ScanClaimRequest,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    return claim_scan_from_qr(
        db=db,
        current_user=current_user,
        qr_raw=payload.qr_raw
    )
