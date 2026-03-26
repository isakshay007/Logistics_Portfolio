#!/usr/bin/env sh

set -eu

SERVICE_NAME="${SERVICE_NAME:-lafl-logistics-portal}"
REGION="${REGION:-us-central1}"
PROJECT_ID="${PROJECT_ID:-}"
APP_BASE_URL="${APP_BASE_URL:-}"
USE_FIRESTORE="${USE_FIRESTORE:-true}"
OPS_API_KEY="${OPS_API_KEY:-}"
SMTP_HOST="${SMTP_HOST:-smtp.gmail.com}"
SMTP_PORT="${SMTP_PORT:-465}"
SMTP_SECURE="${SMTP_SECURE:-true}"
SMTP_USER="${SMTP_USER:-}"
SMTP_PASS="${SMTP_PASS:-}"
MAIL_FROM="${MAIL_FROM:-}"
MAIL_TO="${MAIL_TO:-}"

if [ -z "$PROJECT_ID" ]; then
  PROJECT_ID="$(gcloud config get-value project 2>/dev/null || true)"
fi

if [ -z "$PROJECT_ID" ]; then
  echo "PROJECT_ID is required."
  exit 1
fi

if [ -z "$APP_BASE_URL" ]; then
  APP_BASE_URL="https://${SERVICE_NAME}-${PROJECT_ID}.a.run.app"
fi

gcloud run deploy "$SERVICE_NAME" \
  --source . \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --allow-unauthenticated \
  --set-env-vars "PROJECT_ID=${PROJECT_ID},USE_FIRESTORE=${USE_FIRESTORE},APP_BASE_URL=${APP_BASE_URL},OPS_API_KEY=${OPS_API_KEY},SMTP_HOST=${SMTP_HOST},SMTP_PORT=${SMTP_PORT},SMTP_SECURE=${SMTP_SECURE},SMTP_USER=${SMTP_USER},SMTP_PASS=${SMTP_PASS},MAIL_FROM=${MAIL_FROM},MAIL_TO=${MAIL_TO}"
