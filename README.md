# SWE-Project-Spring-2026

Repository for the SWE final project for the Spring 2026 semester

## Project Structure

```
├── client/          # React frontend (Vite)
├── server/          # Spring Boot backend (Maven)
├── init.sql         # DB seed script (runs on first Docker start)
├── docker-compose.yml
├── .env.example     # Template for environment variables
└── .env             # Your local copy (git-ignored)
```

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

**Or**, if running without Docker:

- Node.js 20+
- Java 17+
- PostgreSQL 16+

## Getting Started (Docker — recommended)

1. **Clone the repo**

   ```bash
   git clone <repo-url>
   cd SWE-Project-Spring-2026
   ```

2. **Create your `.env` file**

   ```bash
   cp .env.example .env
   ```

   The defaults work out of the box. Edit `.env` if you need different database credentials.

3. **Start everything**

   ```bash
   docker compose up --build
   ```

   This starts three containers:

   | Service    | URL                     |
   |------------|-------------------------|
   | Frontend   | http://localhost:5173    |
   | Backend    | http://localhost:8080    |
   | PostgreSQL | localhost:5432           |

4. **Open the app** — visit **http://localhost:5173** in your browser.

5. **Stop everything**

   ```bash
   docker compose down
   ```

   To also wipe the database volume (e.g. to re-run `init.sql`):

   ```bash
   docker compose down -v
   ```

## Environment Variables

All shared config lives in `.env.example`. Copy it to `.env` before running:

| Variable                    | Description                          | Default                                           |
|-----------------------------|--------------------------------------|---------------------------------------------------|
| `POSTGRES_DB`               | Database name                        | `prereq_visualizer`                               |
| `POSTGRES_USER`             | Database username                    | `postgres`                                        |
| `POSTGRES_PASSWORD`         | Database password                    | `postgres`                                        |
| `SPRING_DATASOURCE_URL`     | JDBC connection string               | `jdbc:postgresql://db:5432/prereq_visualizer`     |
| `SPRING_DATASOURCE_USERNAME`| Spring datasource user               | `postgres`                                        |
| `SPRING_DATASOURCE_PASSWORD`| Spring datasource password           | `postgres`                                        |
| `VITE_API_BASE_URL`         | Backend URL for the Vite dev proxy   | `http://localhost:8080`                            |

> **Note:** `.env` is git-ignored. Never commit real credentials. Only `.env.example` is tracked.

## Useful Commands

```bash
# Rebuild after changing a Dockerfile or pom.xml / package.json
docker compose up --build

# View logs for a single service
docker compose logs -f backend

# Open a psql shell inside the running DB container
docker compose exec db psql -U postgres -d prereq_visualizer

# Run backend tests
cd server && ./mvnw test

# Run frontend lint
cd client && npm run lint
```
