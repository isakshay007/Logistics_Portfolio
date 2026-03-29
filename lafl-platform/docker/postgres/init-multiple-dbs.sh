#!/bin/sh
set -eu

# lafl_user is created by POSTGRES_DB in docker-compose.
for db in lafl_shipment lafl_quote; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE "$db";
EOSQL
done

# Create application schemas expected by Flyway/JPA.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "lafl_user" <<-EOSQL
  CREATE SCHEMA IF NOT EXISTS user_service;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "lafl_shipment" <<-EOSQL
  CREATE SCHEMA IF NOT EXISTS shipment_service;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "lafl_quote" <<-EOSQL
  CREATE SCHEMA IF NOT EXISTS quote_service;
EOSQL
