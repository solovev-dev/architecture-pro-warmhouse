import os

from flask import Flask, jsonify, request

from events import DeviceEventPublisher
from service import DeviceService
from storage import DeviceStorage


class DeviceAPI:
    def __init__(self, service: DeviceService) -> None:
        self._service = service
        self.app = Flask(__name__)
        self._register_routes()

    def _register_routes(self) -> None:
        self.app.add_url_rule("/health", view_func=self.health, methods=["GET"])
        self.app.add_url_rule("/api/v1/devices", view_func=self.create_device, methods=["POST"])
        self.app.add_url_rule("/api/v1/devices", view_func=self.list_devices, methods=["GET"])
        self.app.add_url_rule("/api/v1/devices/<device_id>", view_func=self.get_device, methods=["GET"])
        self.app.add_url_rule(
            "/api/v1/devices/<device_id>/state",
            view_func=self.update_state,
            methods=["PATCH"],
        )

    def health(self):
        return jsonify({"status": "ok"})

    def create_device(self):
        payload = request.get_json(silent=True) or {}
        try:
            device = self._service.create_device(
                name=payload.get("name", ""),
                device_type=payload.get("type", ""),
                location=payload.get("location", ""),
                state=payload.get("state", "off"),
            )
        except ValueError as exc:
            return jsonify({"error": str(exc)}), 400
        return jsonify(device.to_dict()), 201

    def list_devices(self):
        devices = [d.to_dict() for d in self._service.list_devices()]
        return jsonify(devices)

    def get_device(self, device_id: str):
        device = self._service.get_device(device_id)
        if device is None:
            return jsonify({"error": "Device not found"}), 404
        return jsonify(device.to_dict())

    def update_state(self, device_id: str):
        payload = request.get_json(silent=True) or {}
        try:
            device = self._service.set_state(device_id, payload.get("state", ""))
        except ValueError as exc:
            return jsonify({"error": str(exc)}), 400
        if device is None:
            return jsonify({"error": "Device not found"}), 404
        return jsonify(device.to_dict())


def create_app() -> Flask:
    database_url = os.getenv(
        "DATABASE_URL",
        "postgres://postgres:postgres@localhost:5432/device_control",
    )
    kafka_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    device_events_topic = os.getenv("DEVICE_EVENTS_TOPIC", "device-events")

    storage = DeviceStorage(database_url)
    publisher = DeviceEventPublisher(kafka_servers, device_events_topic)
    service = DeviceService(storage, publisher)
    return DeviceAPI(service).app
