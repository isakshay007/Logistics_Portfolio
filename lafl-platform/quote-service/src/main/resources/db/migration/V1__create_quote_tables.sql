CREATE SCHEMA IF NOT EXISTS quote_service;

CREATE TABLE IF NOT EXISTS quote_service.quotes (
  id VARCHAR(64) PRIMARY KEY,
  company VARCHAR(255) NOT NULL,
  contact_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  service_type VARCHAR(255) NOT NULL,
  origin VARCHAR(255) NOT NULL,
  destination VARCHAR(255) NOT NULL,
  shipment_type VARCHAR(255) NOT NULL,
  cargo_details TEXT NOT NULL,
  status VARCHAR(64) NOT NULL,
  created_at VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS quote_service.contacts (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  company VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  created_at VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS quote_service.issue_snapshots (
  reference VARCHAR(64) PRIMARY KEY,
  status VARCHAR(128) NOT NULL,
  issue_count INTEGER NOT NULL,
  summary TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_quotes_created_at ON quote_service.quotes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_contacts_created_at ON quote_service.contacts(created_at DESC);
