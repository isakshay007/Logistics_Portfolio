CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE TABLE IF NOT EXISTS shipment_service.shipments (
  id BIGSERIAL PRIMARY KEY,
  reference VARCHAR(64) NOT NULL UNIQUE,
  client VARCHAR(255) NOT NULL,
  mode VARCHAR(128) NOT NULL,
  status VARCHAR(128) NOT NULL,
  progress INTEGER NOT NULL,
  eta VARCHAR(64) NOT NULL,
  current_location VARCHAR(255) NOT NULL,
  destination VARCHAR(255) NOT NULL,
  last_updated VARCHAR(64) NOT NULL,
  summary TEXT NOT NULL,
  next_action TEXT NOT NULL,
  support_owner VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS shipment_service.tracking_events (
  id BIGSERIAL PRIMARY KEY,
  shipment_id BIGINT NOT NULL REFERENCES shipment_service.shipments(id) ON DELETE CASCADE,
  label VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL,
  timestamp VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS shipment_service.tracking_issues (
  id BIGSERIAL PRIMARY KEY,
  shipment_id BIGINT NOT NULL REFERENCES shipment_service.shipments(id) ON DELETE CASCADE,
  severity VARCHAR(32) NOT NULL,
  title VARCHAR(255) NOT NULL,
  detail TEXT NOT NULL,
  owner VARCHAR(255) NOT NULL,
  action TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_shipments_reference
  ON shipment_service.shipments(reference);

CREATE INDEX IF NOT EXISTS idx_tracking_events_shipment_id
  ON shipment_service.tracking_events(shipment_id);

CREATE INDEX IF NOT EXISTS idx_tracking_issues_shipment_id
  ON shipment_service.tracking_issues(shipment_id);
