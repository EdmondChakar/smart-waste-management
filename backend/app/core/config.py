from dotenv import load_dotenv
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent
ENV_FILE = BASE_DIR / ".env"

load_dotenv(ENV_FILE)


class Settings:
    APP_NAME: str = os.getenv("APP_NAME", "Smart Waste Management API")
    APP_VERSION: str = os.getenv("APP_VERSION", "1.0.0")


settings = Settings()