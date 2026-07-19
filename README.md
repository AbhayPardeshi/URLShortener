# URL Shortener - Spring Boot

A URL shortening service built with Spring Boot, Postgres, and Redis, designed as a one-day system-design-to-implementation exercise — covering functional requirements, non-functional requirements, capacity estimation, and architecture before writing any code.

## Features

- **Shorten a URL** — converts a long URL into a short, unique code
- **Custom alias** — optionally specify your own short code instead of an auto-generated one
- **Redirect** — resolves a short code back to its original URL
- **Idempotency** — safely retry a creation request (e.g. after a network failure) without creating duplicate short URLs, via a client-supplied `Idempotency-Key` header
- **Expiration** — optional expiry timestamp on a short URL; expired links return `410 Gone`
- **Click analytics (basic)** — tracks click count per short URL
- **Redis caching** — cache-aside pattern in front of the redirect lookup, since redirects are the dominant read path

## Tech Stack

| Layer | Choice |
|---|---|
| Language / Framework | Java, Spring Boot |
| Database | PostgreSQL (Docker) |
| Cache | Redis (Docker) |
| ORM | Spring Data JPA / Hibernate |
| ID generation | Postgres `BIGSERIAL` + Base62 encoding |
| Config | Environment variables via `.env` (no secrets committed) |

## Architecture

```
Client
  │
  ▼
API Server (Spring Boot)
  │
  ├──► ID Generator (Base62 encode, write path)
  │       │
  │       ▼
  │    Database (Postgres) ◄──┐
  │                            │
  └──► Redis Cache (read path)─┘
```

- **Write path**: Client → API → generate/save short code → Postgres
- **Read path**: Client → API → Redis (hit? return) → miss → Postgres → populate Redis → redirect

## Design Choices

- **Counter-based IDs (Postgres `BIGSERIAL` + Base62)** — atomic, collision-free by construction. Avoids hash-based collision handling and still allows two different short codes for the same long URL.
- **Redis cache-aside** — ~100:1 read:write ratio (below) makes the redirect path the clear caching target.
- **Client-supplied `Idempotency-Key`, not content hashing** — enables safe retries without preventing multiple codes per URL. Same pattern as payment APIs like Stripe.

## Capacity Estimation

100K new URLs/day, 100:1 read:write, 5-year retention → **~350 QPS peak reads**, **~37 GB total storage**, Base62 (7 chars) supports ~3.5T codes vs. ~182.5M needed.

## Database Schema

**`urls`**: `id` (BIGSERIAL PK) · `short_code` (unique, indexed) · `long_url` · `is_custom_alias` · `click_count` · `created_at` · `expires_at` (nullable)

**`idempotency_keys`**: `idempotency_key` (UUID PK) · `response_body` (JSON, replayed on retry) · `created_at`

## API

### `POST /api/v1/urls`
Create a short URL. Requires `Idempotency-Key` header.

**Request**
```json
{
  "longURL": "https://example.com/some/long/path",
  "customAlias": true,
  "shortURL": "my-brand",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

**Response `201 Created`**
```json
{
  "shortURL": "my-brand",
  "longURL": "https://example.com/some/long/path"
}
```

### `GET /{shortCode}`
Redirects to the original URL.
- `302 Found` — success
- `404 Not Found` — short code doesn't exist
- `410 Gone` — short code has expired

## Local Setup

**Prerequisites**: Java 17+, Maven, Docker + Docker Compose

1. Clone the repo
2. Create a `.env` file in the project root:
   ```
   DB_USERNAME=url_admin
   DB_PASSWORD=<your_password>
   ```
3. Start Postgres and Redis:
   ```bash
   docker compose up -d
   ```
4. Run the Spring Boot app (IntelliJ users: configure the **EnvFile** plugin to load `.env` into the run configuration)

