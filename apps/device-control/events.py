import json
import logging
from datetime import datetime, timezone

from kafka import KafkaProducer

from models import Device

logger = logging.getLogger(__name__)


class DeviceEventPublisher:
    """Publishes device lifecycle events to Kafka."""

    def __init__(self, bootstrap_servers: str, topic: str) -> None:
        self._topic = topic
        self._producer = KafkaProducer(
            bootstrap_servers=bootstrap_servers.split(","),
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda v: v.encode("utf-8") if v else None,
            acks="all",
            retries=3,
        )

    def publish_device_created(self, device: Device, source: str = "device-control") -> None:
        event = {
            "eventType": "device.created",
            "deviceId": device.id,
            "name": device.name,
            "type": device.type,
            "location": device.location,
            "state": device.state,
            "source": source,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        }
        future = self._producer.send(self._topic, key=device.id, value=event)
        future.get(timeout=10)
        self._producer.flush()
        logger.info("Published device.created for deviceId=%s to topic=%s", device.id, self._topic)

    def close(self) -> None:
        self._producer.close()
