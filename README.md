# Job Application Tracker API

A REST API for tracking job applications through stages: Wishlist, Applied, OA, Interview, Offer, Rejected.

Built with Java 25, Spring Boot, Spring Data JPA, Spring Security (JWT), PostgreSQL, and Maven.

## Features

- User registration and login (JWT authentication)
- CRUD for job applications, scoped per authenticated user
- Search, status filtering, pagination, and sorting
- Bean Validation on all request bodies
- Centralized exception handling
- Swagger / OpenAPI docs

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5 |
| Persistence | Spring Data JPA + PostgreSQL |
| Auth | Spring Security + JWT (jjwt) |
| Build | Maven |
| Docs | springdoc-openapi (Swagger UI) |
| Containerization | Docker, Docker Compose |
| CI | GitHub Actions |
| Deployment | Railway |

## Running locally with Docker Compose

1. Copy the env file and set a JWT secret:
   ```
   cp .env.example .env
   # generate one with: openssl rand -base64 32
   ```
2. Start everything (API + PostgreSQL):
   ```
   docker compose up --build
   ```
3. API is available at `http://localhost:8080`, Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Running locally without Docker

Requires a local PostgreSQL instance and Java 25.

```
./mvnw spring-boot:run
```

Connection settings default to `localhost:5432/jobtracker` (user/password: `jobtracker`) — override with the `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars. `JWT_SECRET` defaults to a dev-only value in `application.properties`; override it for anything beyond local dev.

## API overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account, returns a JWT |
| POST | `/api/auth/login` | Authenticate, returns a JWT |
| POST | `/api/job-applications` | Create a job application |
| GET | `/api/job-applications/{id}` | Get one job application |
| PUT | `/api/job-applications/{id}` | Update a job application |
| DELETE | `/api/job-applications/{id}` | Delete a job application |
| GET | `/api/job-applications` | Search/filter/paginate/sort (`keyword`, `status`, `page`, `size`, `sort`) |

All `/api/job-applications/**` endpoints require `Authorization: Bearer <token>`.

## Tests / CI

GitHub Actions (`.github/workflows/ci.yml`) runs `./mvnw verify` against a PostgreSQL service container on every push/PR to `main`.

## Deployment (Railway)

Railway builds from the repo's `Dockerfile` (see `railway.json`). Configure these environment variables in the Railway project:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — pointing at a provisioned Postgres instance (e.g. Railway's Postgres plugin)
- `JWT_SECRET` — a strong, unique secret (`openssl rand -base64 32`)
- `JWT_EXPIRATION_MS` — optional, defaults to 24h

Railway injects `PORT` automatically; the app already binds to it via `server.port=${PORT:8080}`.
