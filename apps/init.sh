#!/bin/bash

# Exit on any error
set -e

cd "$(dirname "$0")"

COMPOSE="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  COMPOSE="docker-compose"
fi

echo "Starting the Smart Home Sensor API..."
echo "Building and starting containers..."
$COMPOSE up --build -d

echo "Waiting for services to be ready..."
# Wait for PostgreSQL to be ready
for i in {1..30}; do
  if docker exec smarthome-postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "PostgreSQL is ready!"
    break
  fi
  echo "Waiting for PostgreSQL to start... ($i/30)"
  sleep 1
done

# Check if PostgreSQL is ready
if ! docker exec smarthome-postgres pg_isready -U postgres > /dev/null 2>&1; then
  echo "Error: PostgreSQL did not start within the expected time."
  exit 1
fi

# Wait for the API health endpoint
for i in {1..30}; do
  if curl -sf http://localhost:8080/health > /dev/null 2>&1; then
    echo "Smart Home API is ready!"
    break
  fi
  echo "Waiting for Smart Home API... ($i/30)"
  sleep 1
done

if ! curl -sf http://localhost:8080/health > /dev/null 2>&1; then
  echo "Error: Smart Home API did not become healthy in time."
  echo "Check logs with: $COMPOSE logs -f"
  exit 1
fi

echo "All services are up and running!"
echo "The API is available at http://localhost:8080"
echo "Temperature API is available at http://localhost:8081"

echo "To stop the services, run: $COMPOSE down"