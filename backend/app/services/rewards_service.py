from fastapi import HTTPException, status
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.schemas.rewards import RewardCreate, RewardUpdate


def _serialize_reward_row(row) -> dict:
    return {
        "reward_id": row[0],
        "title": row[1],
        "description": row[2],
        "points_cost": row[3],
        "is_active": row[4]
    }


def list_rewards(db: Session, active_only: bool = False) -> dict:
    if active_only:
        query = text("""
            SELECT reward_id, title, description, points_cost, is_active
            FROM rewards
            WHERE is_active = TRUE
            ORDER BY reward_id;
        """)
        result = db.execute(query)
    else:
        query = text("""
            SELECT reward_id, title, description, points_cost, is_active
            FROM rewards
            ORDER BY reward_id;
        """)
        result = db.execute(query)

    rewards = [_serialize_reward_row(row) for row in result.fetchall()]

    return {
        "count": len(rewards),
        "rewards": rewards
    }


def get_reward_by_id(db: Session, reward_id: int) -> dict | None:
    query = text("""
        SELECT reward_id, title, description, points_cost, is_active
        FROM rewards
        WHERE reward_id = :reward_id;
    """)
    row = db.execute(query, {"reward_id": reward_id}).fetchone()

    if row is None:
        return None

    return _serialize_reward_row(row)


def create_reward_record(db: Session, reward_data: RewardCreate) -> dict:
    duplicate_query = text("""
        SELECT reward_id
        FROM rewards
        WHERE LOWER(title) = LOWER(:title);
    """)
    existing_reward = db.execute(
        duplicate_query,
        {"title": reward_data.title}
    ).fetchone()

    if existing_reward is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A reward with this title already exists"
        )

    insert_query = text("""
        INSERT INTO rewards (title, description, points_cost, is_active)
        VALUES (:title, :description, :points_cost, :is_active)
        RETURNING reward_id, title, description, points_cost, is_active;
    """)
    result = db.execute(
        insert_query,
        {
            "title": reward_data.title,
            "description": reward_data.description,
            "points_cost": reward_data.points_cost,
            "is_active": reward_data.is_active
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "Reward created successfully",
        "reward": _serialize_reward_row(row)
    }


def update_reward_record(db: Session, reward_id: int, reward_data: RewardUpdate) -> dict:
    existing_reward = get_reward_by_id(db, reward_id)

    if existing_reward is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Reward not found"
        )

    duplicate_query = text("""
        SELECT reward_id
        FROM rewards
        WHERE LOWER(title) = LOWER(:title)
          AND reward_id <> :reward_id;
    """)
    duplicate_reward = db.execute(
        duplicate_query,
        {
            "title": reward_data.title,
            "reward_id": reward_id
        }
    ).fetchone()

    if duplicate_reward is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A reward with this title already exists"
        )

    update_query = text("""
        UPDATE rewards
        SET
            title = :title,
            description = :description,
            points_cost = :points_cost,
            is_active = :is_active
        WHERE reward_id = :reward_id
        RETURNING reward_id, title, description, points_cost, is_active;
    """)
    result = db.execute(
        update_query,
        {
            "reward_id": reward_id,
            "title": reward_data.title,
            "description": reward_data.description,
            "points_cost": reward_data.points_cost,
            "is_active": reward_data.is_active
        }
    )
    db.commit()

    row = result.fetchone()

    return {
        "message": "Reward updated successfully",
        "reward": _serialize_reward_row(row)
    }
