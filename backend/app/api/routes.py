from fastapi import APIRouter

router = APIRouter()


@router.get("/")
def root():
    return {"message": "Smart Waste Management backend is running"}


@router.get("/health")
def health_check():
    return {"status": "ok"}