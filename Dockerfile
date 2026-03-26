FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

COPY . .
COPY --from=frontend-build /app/frontend/dist/frontend/browser ./frontend/dist/frontend/browser

ENV NODE_ENV=production
ENV PORT=8080

EXPOSE 8080

CMD ["node", "index.js"]
