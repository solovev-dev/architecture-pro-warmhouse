package services

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/segmentio/kafka-go"
)

// DeviceCreatedEvent is published when a sensor/device is created.
type DeviceCreatedEvent struct {
	EventType string `json:"eventType"`
	DeviceID  string `json:"deviceId"`
	Name      string `json:"name"`
	Type      string `json:"type"`
	Location  string `json:"location"`
	State     string `json:"state,omitempty"`
	Source    string `json:"source"`
	Timestamp string `json:"timestamp"`
}

// DeviceEventPublisher publishes device lifecycle events to Kafka.
type DeviceEventPublisher struct {
	writer *kafka.Writer
	topic  string
}

// NewDeviceEventPublisher creates a Kafka publisher for device events.
func NewDeviceEventPublisher(brokers []string, topic string) *DeviceEventPublisher {
	writer := &kafka.Writer{
		Addr:         kafka.TCP(brokers...),
		Topic:        topic,
		Balancer:     &kafka.LeastBytes{},
		RequiredAcks: kafka.RequireOne,
		Async:        false,
	}
	return &DeviceEventPublisher{writer: writer, topic: topic}
}

// PublishDeviceCreated publishes a device.created event.
func (p *DeviceEventPublisher) PublishDeviceCreated(deviceID, name, deviceType, location string) error {
	event := DeviceCreatedEvent{
		EventType: "device.created",
		DeviceID:  deviceID,
		Name:      name,
		Type:      deviceType,
		Location:  location,
		State:     "off",
		Source:    "smart_home",
		Timestamp: time.Now().UTC().Format(time.RFC3339),
	}

	payload, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("marshal device.created event: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	err = p.writer.WriteMessages(ctx, kafka.Message{
		Key:   []byte(deviceID),
		Value: payload,
	})
	if err != nil {
		return fmt.Errorf("publish device.created to %s: %w", p.topic, err)
	}

	log.Printf("Published device.created for deviceId=%s to topic=%s", deviceID, p.topic)
	return nil
}

// Close closes the underlying Kafka writer.
func (p *DeviceEventPublisher) Close() error {
	if p.writer == nil {
		return nil
	}
	return p.writer.Close()
}
