from typing import Optional

import psycopg2
import psycopg2.extras

from models import Device


class DeviceStorage:
    """PostgreSQL-backed device store."""

    def __init__(self, database_url: str) -> None:
        self._database_url = database_url
        self._ensure_schema()

    def _connect(self):
        return psycopg2.connect(self._database_url)

    def _ensure_schema(self) -> None:
        with self._connect() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    CREATE TABLE IF NOT EXISTS devices (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        location VARCHAR(100) NOT NULL,
                        state VARCHAR(20) NOT NULL DEFAULT 'off',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """
                )
            conn.commit()

    def create(self, name: str, device_type: str, location: str, state: str = "off") -> Device:
        with self._connect() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    """
                    INSERT INTO devices (name, type, location, state)
                    VALUES (%s, %s, %s, %s)
                    RETURNING id, name, type, location, state, created_at
                    """,
                    (name, device_type, location, state),
                )
                row = cur.fetchone()
            conn.commit()
        return self._to_device(row)

    def get(self, device_id: str) -> Optional[Device]:
        numeric_id = self._parse_id(device_id)
        if numeric_id is None:
            return None
        with self._connect() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    """
                    SELECT id, name, type, location, state, created_at
                    FROM devices
                    WHERE id = %s
                    """,
                    (numeric_id,),
                )
                row = cur.fetchone()
        return self._to_device(row) if row else None

    def list_all(self) -> list[Device]:
        with self._connect() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    """
                    SELECT id, name, type, location, state, created_at
                    FROM devices
                    ORDER BY id
                    """
                )
                rows = cur.fetchall()
        return [self._to_device(row) for row in rows]

    def update_state(self, device_id: str, state: str) -> Optional[Device]:
        numeric_id = self._parse_id(device_id)
        if numeric_id is None:
            return None
        with self._connect() as conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    """
                    UPDATE devices
                    SET state = %s
                    WHERE id = %s
                    RETURNING id, name, type, location, state, created_at
                    """,
                    (state, numeric_id),
                )
                row = cur.fetchone()
            conn.commit()
        return self._to_device(row) if row else None

    @staticmethod
    def _parse_id(device_id: str) -> Optional[int]:
        raw = device_id.removeprefix("dc-") if device_id.startswith("dc-") else device_id
        try:
            return int(raw)
        except (TypeError, ValueError):
            return None

    @staticmethod
    def _to_device(row) -> Device:
        created_at = row["created_at"]
        if hasattr(created_at, "isoformat"):
            created_at = created_at.isoformat()
        return Device(
            id=f"dc-{row['id']}",
            name=row["name"],
            type=row["type"],
            location=row["location"],
            state=row["state"],
            created_at=str(created_at),
        )
