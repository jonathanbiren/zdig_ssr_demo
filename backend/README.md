# Backend

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Database Setup](#database-setup)
3. [Flyway: Schema Version Control](#flyway-schema-version-control)
4. [Development Workflow](#development-workflow)
5. [Project Structure](#project-structure)

---

## Architecture Overview

This backend uses hexagonal architecture (ports and adapters) with a single bounded context (`katalog`) to start. The core idea: business logic in the domain and application layers has zero knowledge of the web framework, the database technology, or any other infrastructure concern. Those concerns live in the adapter layer.

```
adapter/in  (web controllers)
    │
    ▼
application/port/in  (use case interfaces)
    │
    ▼
application/service  (orchestration -- no business logic here)
    │
    ▼
domain/model  (aggregates, value objects, domain events -- pure Kotlin, no Spring)
    │
    ▼
application/port/out  (repository interfaces)
    │
    ▼
adapter/out  (Spring Data JDBC -- the only place the database is mentioned)
    │
    ▼
PostgreSQL
```

---

## Database Setup

PostgreSQL runs in a Docker container defined in `docker-compose.yml` at the project root.

**Start the database:**
```bash
# From the project root (soldaten-wohnen/)
docker compose up -d
```

**Stop the database (data is preserved):**
```bash
docker compose stop
```

**Destroy the database completely (wipes all data):**
```bash
docker compose down -v
```

The `-v` flag removes the Docker volume where PostgreSQL stores its data files. Without `-v`, data persists across container restarts indefinitely.

**Connection details** (used by Spring Boot and by the IntelliJ database panel):

| Setting | Value |
|---------|-------|
| Host | `localhost` |
| Port | `5432` |
| Database | `soldaten_wohnen` |
| Username | `soldaten_wohnen` |
| Password | `soldaten_wohnen` |

These values live in `src/main/resources/application.properties` and mirror the environment variables in `docker-compose.yml`.

---

## Flyway: Schema Version Control

### What it does and why it exists

`@Table("wohnungen")` in `WohnungRepositoryAdapter.kt` does **not** create the database table. It is a read instruction: it tells Spring Data JDBC which table to use when persisting or loading a `WohnungRow` (a model of 'Wohnung' which is specific to the persistence layer. One could say the input model for the database). If the table does not exist, you get a runtime error.

Flyway is responsible for creating and evolving the schema. It is a schema version control system: every change to the database structure is written as a numbered SQL file (a "migration"). Flyway tracks which migrations have been applied and runs new ones automatically on startup.

### File locations

```
backend/
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/          ← ALL migration files live here
                    ├── V1__create_wohnungen.sql
                    └── V2__seed_wohnungen.sql
```

**This is the only directory you ever need to touch when changing the schema.**

### Naming convention

Every migration file must follow this exact pattern:

```
V{version}__{description}.sql
  │          │
  │          └── Human-readable description (underscores become spaces in logs)
  └── Integer version number (determines execution order)

Note the DOUBLE underscore between version and description.
```

Examples:
```
V1__create_wohnungen.sql
V2__seed_wohnungen.sql
V3__add_balkon_to_wohnungen.sql
V4__create_bewerbungen.sql
V5__add_index_on_bewerbungen_status.sql
```

Version numbers must be unique and increase monotonically. Gaps are allowed (`V1`, `V3`, `V5` is fine). The number does not need to be sequential -- many teams use timestamps instead (`V20260322143000__create_wohnungen.sql`). For a solo developer, simple integers are clearest.

### The iron rule: never edit an applied migration

Once a migration file has been applied to any database (local, staging, production), **never edit it**. Flyway stores a checksum of every applied file. If you edit one, Flyway will refuse to start with a checksum mismatch error.

If you made a mistake in a migration that has already been applied:
- Write a new migration (`V3__fix_something.sql`) that corrects it.

If you made a mistake in a migration that exists only on your local machine and has never been committed to git, you can:
1. Run `docker compose down -v` to destroy your local database entirely.
2. Delete the incorrect migration file.
3. Write the corrected one.
4. Run `docker compose up -d` and restart Spring Boot. Flyway runs from scratch.

### What Flyway does on startup

```
Spring Boot starts
    │
    ▼
Flyway connects to PostgreSQL using the same datasource as the app
    │
    ▼
Flyway looks for the flyway_schema_history table inside the Postgres database
    ├── If it does not exist: creates it (this is the first ever run)
    └── If it exists: reads which migrations have already been applied
    │
    ▼
Flyway scans src/main/resources/db/migration/ for V*.sql files
    │
    ▼
Flyway runs any migrations not yet recorded in the flyway_schema_history of the Postgres Database, in version order
    │
    ▼
Spring Boot finishes starting and begins accepting requests
```

So before the application accepts any requests, Flyway has run all migrations on your database that have not yet been applied.
The `flyway_schema_history` table lives in your PostgreSQL database. You can inspect it at any time in IntelliJ's database panel to see exactly which migrations have been applied and when.

### How Flyway relates to Docker

Flyway does not talk to Docker. Flyway talks to PostgreSQL. Docker is simply where PostgreSQL happens to be running locally. The connection goes:

```
Flyway → localhost:5432 → Docker port mapping → PostgreSQL inside container
```

If you deployed this application to a server with a real PostgreSQL installation tomorrow, Flyway would work identically. You would just change the URL in `application.properties`. The migration files themselves are portable.

---

## Development Workflow

### Day-to-day startup

```bash
# Terminal 1: start the database (if not already running)
docker compose up -d

# Terminal 2: start the backend (from the backend/ directory)
./mvnw spring-boot:run
# Flyway runs any pending migrations automatically, then the app starts on :8080

# Terminal 3: start the frontend (from the frontend/ directory)
pnpm dev
# Frontend starts on :3000, proxies /api/* requests to :8080
```

### Adding a new feature that requires a schema change

```bash
# 1. Write your Kotlin code (new WohnungRow field, new aggregate, etc.)

# 2. Create a new migration file
#    backend/src/main/resources/db/migration/V3__your_description.sql

# 3. Write the SQL inside it, e.g.:
#    ALTER TABLE wohnungen ADD COLUMN hat_balkon BOOLEAN NOT NULL DEFAULT false;

# 4. Restart Spring Boot
./mvnw spring-boot:run
# Flyway sees V3 has not been applied, runs it, schema now matches your code
```

You never run SQL manually against the database to change the schema. Everything goes through a migration file.

### Resetting your local database to a clean state

Useful when you want to start over (e.g. after experimenting with migrations):

```bash
# Stop containers and destroy the data volume
docker compose down -v

# Start a fresh container (empty database)
docker compose up -d

# Start the backend -- Flyway re-runs all migrations from V1 onwards
./mvnw spring-boot:run
```

---

## Project Structure

```
backend/
├── pom.xml                                         ← Maven dependencies (Spring Boot, Spring Data JDBC,
│                                                     PostgreSQL driver, Flyway)
│
└── src/
    └── main/
        ├── kotlin/
        │   └── org/soldatenwohnen/
        │       ├── SoldatenWohnenApplication.kt    ← Spring Boot entry point
        │       ├── WebConfig.kt                    ← CORS configuration
        │       │
        │       └── katalog/                        ← Bounded context: apartment catalogue
        │           ├── adapter/
        │           │   ├── in/                     ← HTTP controllers (inbound adapters)
        │           │   │   ├── WohnungenAuflistenController.kt
        │           │   │   └── WohnungDetailAnzeigenController.kt
        │           │   └── out/                    ← Database adapters (outbound adapters)
        │           │       └── WohnungRepositoryAdapter.kt  ← WohnungRow (@Table), WohnungJdbcRepository,
        │           │                                           WohnungRepositoryAdapter
        │           ├── application/
        │           │   ├── port/
        │           │   │   ├── in/                 ← Use case interfaces (inbound ports)
        │           │   │   │   ├── WohnungenAuflistenUseCase.kt
        │           │   │   │   └── WohnungDetailAnzeigenUseCase.kt
        │           │   │   └── out/                ← Repository interfaces (outbound ports)
        │           │   │       └── WohnungRepository.kt
        │           │   └── service/                ← Application services (implement use cases)
        │           │       └── WohnungssucheService.kt
        │           └── domain/
        │               └── model/                  ← Aggregate roots, value objects, domain events
        │                   └── Wohnung.kt          ← Wohnung aggregate, WohnungId, Mietpreis, Adresse
        │
        └── resources/
            ├── application.properties              ← datasource URL, Flyway config
            └── db/
                └── migration/                      ← ALL Flyway migration files live here
                    ├── V1__create_wohnungen.sql     ← Creates the wohnungen table and index
                    └── V2__seed_wohnungen.sql       ← Inserts development test data
```

### Key relationships between files

**To add a new column to an existing table:**
- Add a new `V{n}__*.sql` in `db/migration/`
- Add the corresponding field to `WohnungRow` in `adapter/out/WohnungRepositoryAdapter.kt`
- Update `toDomain()` if the domain model needs the new data

**To add a new aggregate:**
- Write a new `V{n}__create_{table}.sql` migration
- Create a new `*Row` class and `*JdbcRepository` interface in `adapter/out/`
- Create a new `*Repository` outbound port interface in `application/port/out/`
- Create an adapter class implementing the port
- Add the domain aggregate in `domain/model/`
- Add use case interfaces in `application/port/in/`
- Add an application service in `application/service/`
- Add a controller in `adapter/in/`
