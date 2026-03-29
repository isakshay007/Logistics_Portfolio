#!/usr/bin/env bash
set -euo pipefail
BASE="http://localhost:8080"
for i in $(seq 1 30); do
  code=$(curl -s -o /tmp/gw-health.json -w "%{http_code}" "$BASE/api/health" || true)
  if [ "$code" = "200" ]; then
    break
  fi
  sleep 3
done
printf "HEALTH_CODE=%s\n" "$code"
cat /tmp/gw-health.json || true
printf "\n"
TRACK_CODE=$(curl -s -o /tmp/track.json -w "%{http_code}" "$BASE/api/v1/shipments/track?reference=LAFL-24017" || true)
printf "TRACK_CODE=%s\n" "$TRACK_CODE"
cat /tmp/track.json || true
printf "\n"
EMAIL="audit2+$(date +%s)@example.com"
SIGNUP_CODE=$(curl -s -o /tmp/signup.json -w "%{http_code}" -X POST "$BASE/api/v1/auth/signup" -H "Content-Type: application/json" -d "{\"name\":\"Audit2 User\",\"email\":\"$EMAIL\",\"company\":\"LAFL\",\"phone\":\"1234567890\",\"interest\":\"demo\",\"password\":\"Password123\"}" || true)
printf "SIGNUP_CODE=%s EMAIL=%s\n" "$SIGNUP_CODE" "$EMAIL"
cat /tmp/signup.json || true
printf "\n"
QUOTE_CODE=$(curl -s -o /tmp/quote.json -w "%{http_code}" -X POST "$BASE/api/v1/quotes" -H "Content-Type: application/json" -d '{"company":"LAFL Demo","contactName":"Akshay","email":"demo@example.com","serviceType":"Air","origin":"NYC","destination":"Toronto","shipmentType":"Pallet","cargoDetails":"Electronics"}' || true)
printf "QUOTE_CODE=%s\n" "$QUOTE_CODE"
cat /tmp/quote.json || true
printf "\n"
CONTACT_CODE=$(curl -s -o /tmp/contact.json -w "%{http_code}" -X POST "$BASE/api/v1/contacts" -H "Content-Type: application/json" -d '{"name":"Akshay","email":"demo@example.com","company":"LAFL Demo","message":"Need enterprise quote"}' || true)
printf "CONTACT_CODE=%s\n" "$CONTACT_CODE"
cat /tmp/contact.json || true
printf "\n"
MAIL_COUNT=$(curl -s http://localhost:8025/api/v1/messages | jq 'length')
printf "MAILHOG_COUNT=%s\n" "$MAIL_COUNT"
curl -s http://localhost:8025/api/v1/messages | jq '.[0:5] | map({from:.Content.Headers.From[0],to:.Content.Headers.To[0],subject:.Content.Headers.Subject[0]})'
