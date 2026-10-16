package main

import (
	"encoding/json"
	"log"
	"math/rand"
	"net/http"
	"os"
	"strings"
	"time"
)

type TemperatureResponse struct {
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Timestamp   time.Time `json:"timestamp"`
	Location    string    `json:"location"`
	Status      string    `json:"status"`
	SensorID    string    `json:"sensor_id"`
	SensorType  string    `json:"sensor_type"`
	Description string    `json:"description"`
}

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/temperature", handleTemperatureByLocation)
	mux.HandleFunc("/temperature/", handleTemperatureByID)
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	})

	addr := getEnv("PORT", ":8081")
	log.Printf("temperature-api starting on %s", addr)
	if err := http.ListenAndServe(addr, mux); err != nil {
		log.Fatalf("server failed: %v", err)
	}
}

func handleTemperatureByLocation(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	location := r.URL.Query().Get("location")
	sensorID := ""
	if location == "" {
		location = "Unknown"
	}
	sensorID = sensorIDFromLocation(location)

	writeJSON(w, http.StatusOK, buildResponse(location, sensorID))
}

func handleTemperatureByID(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	sensorID := strings.TrimPrefix(r.URL.Path, "/temperature/")
	if sensorID == "" || strings.Contains(sensorID, "/") {
		http.Error(w, "sensor id is required", http.StatusBadRequest)
		return
	}

	location := locationFromSensorID(sensorID)
	writeJSON(w, http.StatusOK, buildResponse(location, sensorID))
}

func buildResponse(location, sensorID string) TemperatureResponse {
	value := 15 + rand.Float64()*15 // 15.0 .. 30.0
	return TemperatureResponse{
		Value:       round1(value),
		Unit:        "°C",
		Timestamp:   time.Now().UTC(),
		Location:    location,
		Status:      "active",
		SensorID:    sensorID,
		SensorType:  "temperature",
		Description: "Random temperature reading",
	}
}

func locationFromSensorID(sensorID string) string {
	switch sensorID {
	case "1":
		return "Living Room"
	case "2":
		return "Bedroom"
	case "3":
		return "Kitchen"
	default:
		return "Unknown"
	}
}

func sensorIDFromLocation(location string) string {
	switch location {
	case "Living Room":
		return "1"
	case "Bedroom":
		return "2"
	case "Kitchen":
		return "3"
	default:
		return "0"
	}
}

func round1(v float64) float64 {
	return float64(int(v*10+0.5)) / 10
}

func writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	if err := json.NewEncoder(w).Encode(payload); err != nil {
		log.Printf("failed to write response: %v", err)
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
