#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE device_control;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /schemas/smarthome.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname device_control -f /schemas/device_control.sql
