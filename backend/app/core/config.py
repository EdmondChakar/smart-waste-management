from dotenv import load_dotenv
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent
ENV_FILE = BASE_DIR / ".env"

load_dotenv(ENV_FILE)


class Settings:
    APP_NAME: str = os.getenv("APP_NAME", "Smart Waste Management API")
    APP_VERSION: str = os.getenv("APP_VERSION", "1.0.0")

    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: str = os.getenv("DB_PORT", "5432")
    DB_NAME: str = os.getenv("DB_NAME", "smart_waste")
    DB_USER: str = os.getenv("DB_USER", "postgres")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "")
    JWT_SECRET_KEY: str = os.getenv("JWT_SECRET_KEY", "change-this-in-production")
    JWT_ALGORITHM: str = os.getenv("JWT_ALGORITHM", "HS256")
    ACCESS_TOKEN_EXPIRE_MINUTES: int = int(
        os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "60")
    )
    CORS_ORIGINS: str = os.getenv("CORS_ORIGINS", "")
    BIN_FULL_THRESHOLD_PCT: float = float(
        os.getenv("BIN_FULL_THRESHOLD_PCT", "80")
    )
    BIN_FULL_THRESHOLD_KG: float = float(
        os.getenv("BIN_FULL_THRESHOLD_KG", "4.5")
    )
    ADMIN_SIGNUP_CODE: str = os.getenv("ADMIN_SIGNUP_CODE", "")

    @property
    def DATABASE_URL(self) -> str:
        return (
            f"postgresql+psycopg2://{self.DB_USER}:{self.DB_PASSWORD}"
            f"@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
        )

    @property
    def CORS_ORIGINS_LIST(self) -> list[str]:
        return [
            origin.strip()
            for origin in self.CORS_ORIGINS.split(",")
            if origin.strip()
        ]


settings = Settings()
