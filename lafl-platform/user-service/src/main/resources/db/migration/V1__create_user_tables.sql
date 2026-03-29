CREATE SCHEMA IF NOT EXISTS user_service;

CREATE TABLE IF NOT EXISTS user_service.users (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  company VARCHAR(255) NOT NULL,
  phone VARCHAR(64) NOT NULL,
  interest VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_service.user_roles (
  user_id VARCHAR(64) NOT NULL REFERENCES user_service.users(id) ON DELETE CASCADE,
  role VARCHAR(64) NOT NULL,
  PRIMARY KEY(user_id, role)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON user_service.users(email);
