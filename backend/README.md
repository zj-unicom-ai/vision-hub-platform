# Vision Hub Backend

This directory contains the Java backend for the open-source Vision Hub Platform.

## Modules

```text
backend/
├── vision-hub-common  # shared utilities, enums, and common models
├── vision-hub-web     # platform API service
└── vision-hub-media   # media and stream helper service
```

## Requirements

- JDK 17+
- Maven 3.8+ or the included Maven wrapper
- MySQL 8+
- Redis
- Kafka
- MinIO

## Database

Create a database named `vision_hub`, then import:

```bash
mysql -u root -p -e "CREATE DATABASE vision_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
mysql -u root -p vision_hub < vision-hub-web/vision-hub-web-server/src/main/resources/sql/schema.sql
```

The schema seeds the default console account `unicom` / `ZJ_Unicom` (BCrypt-hashed).

## Configuration

The backend reads configuration from environment variables. Defaults are suitable for local development; all project-wide default passwords are `ZJ_Unicom`.

```bash
export DB_URL="jdbc:mysql://localhost:3306/vision_hub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true"
export DB_USERNAME="root"
export DB_PASSWORD="ZJ_Unicom"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export REDIS_PASSWORD="ZJ_Unicom"
export KAFKA_SERVERS="localhost:9092"
export MINIO_ENDPOINT="http://localhost:9000"
export MINIO_BUCKET="vision-hub"
export MINIO_ACCESS_KEY="ZJ_Unicom"
export MINIO_SECRET_KEY="ZJ_Unicom"
export INTERNAL_SECRET="ZJ_Unicom"
export JWT_SECRET="ZJ_Unicom"
export WEB_SERVER_PORT="18080"
export MEDIA_SERVER_PORT="18081"
```

## Build

```bash
./mvnw clean package -DskipTests
```

## Run

Run the web API service:

```bash
./mvnw -pl vision-hub-web/vision-hub-web-server -am spring-boot:run
```

Run the media service:

```bash
./mvnw -pl vision-hub-media/vision-hub-media-server -am spring-boot:run
```

## Docker Deployment

The all-in-one Docker Compose stack (frontend, web, media, MySQL, Redis, Kafka, MinIO) lives in the repository-level [`docker/`](../docker) directory:

```bash
cd ../docker
cp .env.example .env
# Edit .env to change passwords, ports, and the MinIO CDN address.
docker compose up -d --build
```

Both backend images are built from `docker/backend.Dockerfile` (multi-stage Maven build; Java 17 code running on a Temurin 21 JRE).

## Open-Source Scope

Scene governance and device organization modules are not part of this edition. Device APIs only manage device access information, and task APIs bind devices directly to algorithms.
