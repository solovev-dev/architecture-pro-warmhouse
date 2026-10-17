import logging
from typing import Optional

from events import DeviceEventPublisher
from models import Device
from storage import DeviceStorage

logger = logging.getLogger(__name__)

ALLOWED_STATES = {"on", "off"}


class DeviceService:
    def __init__(self, storage: DeviceStorage, event_publisher: DeviceEventPublisher) -> None:
        self._storage = storage
        self._events = event_publisher

    def create_device(self, name: str, device_type: str, location: str, state: str = "off") -> Device:
        if not name or not device_type or not location:
            raise ValueError("name, type, and location are required")
        normalized_state = (state or "off").lower()
        if normalized_state not in ALLOWED_STATES:
            raise ValueError(f"state must be one of: {', '.join(sorted(ALLOWED_STATES))}")

        device = self._storage.create(name, device_type, location, normalized_state)
        try:
            self._events.publish_device_created(device)
        except Exception as exc:
            logger.warning("Failed to publish device.created for id=%s: %s", device.id, exc)
        return device

    def get_device(self, device_id: str) -> Optional[Device]:
        return self._storage.get(device_id)

    def list_devices(self) -> list[Device]:
        return self._storage.list_all()

    def set_state(self, device_id: str, state: str) -> Optional[Device]:
        normalized_state = (state or "").lower()
        if normalized_state not in ALLOWED_STATES:
            raise ValueError(f"state must be one of: {', '.join(sorted(ALLOWED_STATES))}")
        return self._storage.update_state(device_id, normalized_state)
