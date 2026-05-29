# Soldaten Wohnen Backend

This is the Spring Boot application for Soldaten Wohnen. It serves both the backend API and the server-rendered HTML user interface.

There is no separate JavaScript frontend project anymore. Pages are rendered with Thymeleaf, small interactive updates use HTMX, styling is generated with Tailwind CSS v4, and the application data is stored in PostgreSQL.

## Contents

1. [Current Stack](#current-stack)
2. [Quick Start](#quick-start)
3. [Daily Development Workflow](#daily-development-workflow)
4. [Application Routes](#application-routes)
5. [Database And Flyway](#database-and-flyway)
6. [Architecture](#architecture)
7. [Project Structure](#project-structure)
8. [Common Tasks](#common-tasks)
9. [Troubleshooting](#troubleshooting)

## Current Stack

The application is intentionally backend-centered:

| Concern | Technology | Where it lives |
|---|---|---|
| Main language | Kotlin | `src/main/kotlin` |
| Web framework | Spring Boot Web MVC | Controllers in `adapter/in` |
| HTML rendering | Thymeleaf | `src/main/resources/templates` |
| HTML fragment updates | HTMX | Served from a WebJar under `/webjars/**` |
| Styling | Tailwind CSS v4 | Source in `assets/styles.css`, generated CSS in `static/css/app.css` |
| Database access | Spring Data JDBC | `adapter/out/WohnungRepositoryAdapter.kt` |
| Database | PostgreSQL | Docker Compose at `../docker-compose.yml` |
| Schema migrations | Flyway | `src/main/resources/db/migration` |
| Build tool | Maven Wrapper | `./mvnw` |

Important consequence: running the application does not require Node, Vite, a frontend dev server, or browser-side routing. Spring Boot serves the HTML and CSS directly on `localhost:8080`.

## Quick Start

These commands assume your terminal is in the `backend/` directory.

Start PostgreSQL:

```bash
docker compose -f ../docker-compose.yml up -d
```

Run the application:

```bash
./mvnw spring-boot:run
```

Open the catalogue page:

```text
http://localhost:8080/wohnungen
```

Run the test suite:

```bash
./mvnw test
```

Stop PostgreSQL when you are done:

```bash
docker compose -f ../docker-compose.yml stop
```

## Daily Development Workflow

### 1. Start The Database

The application expects PostgreSQL to be available at `localhost:5432`. Start it before starting Spring Boot:

```bash
docker compose -f ../docker-compose.yml up -d
```

This starts the `postgres:17` container defined in the repository root. Data is kept in a Docker volume, so stopping and starting the container does not wipe the database.

### 2. Start Spring Boot

From `backend/`:

```bash
./mvnw spring-boot:run
```

On startup, Spring Boot will:

1. Connect to PostgreSQL.
2. Run any pending Flyway migrations.
3. Create the Spring Data JDBC repositories.
4. Start the web server on `localhost:8080`.

### 3. Open The App

The public HTML UI is here:

```text
http://localhost:8080/wohnungen
```

The home route `/` renders the same catalogue page.

### 4. Rebuild CSS Only When Needed

The generated CSS file is committed at `src/main/resources/static/css/app.css`. You only need to rebuild it after changing Tailwind classes in templates or changing `src/main/resources/assets/styles.css`.

From `backend/`:

```bash
pnpm dlx @tailwindcss/cli@4.3.0 -i ./src/main/resources/assets/styles.css -o ./src/main/resources/static/css/app.css --minify
```

This command uses `pnpm dlx` to run the Tailwind CLI without adding a `package.json` to the project. Node is optional for normal backend work, but required for regenerating Tailwind CSS.

### 5. Run Tests Before Finishing Work

The current test starts the full Spring application context, so PostgreSQL must be running:

```bash
./mvnw test
```

For a faster compile-only check that does not run tests:

```bash
./mvnw -DskipTests compile
```

## Application Routes

### Public HTML Routes

These routes are rendered with Thymeleaf and are public:

| Route | Purpose |
|---|---|
| `GET /` | Catalogue page |
| `GET /wohnungen` | Catalogue page |
| `GET /wohnungen/suche?q=...` | HTMX fragment for the filtered apartment list |
| `GET /wohnungen/{id}` | Apartment detail page |
| `GET /wohnungen/{id}/kontakt` | HTMX fragment for contact details |
| `GET /css/app.css` | Generated Tailwind stylesheet |
| `GET /webjars/**` | WebJar assets, including HTMX |

### API Routes

The JSON API routes still exist:

| Route | Purpose |
|---|---|
| `GET /api/katalog/wohnungen` | Published apartment list as JSON |
| `GET /api/katalog/wohnungen/{id}` | Apartment detail as JSON |

API routes are protected by Spring Security. During local development the credentials come from `application.properties`:

```text
Username: biren
Password: biren
```

Example API call:

```bash
curl -u biren:biren http://localhost:8080/api/katalog/wohnungen
```

## Database And Flyway

### Local Database

PostgreSQL runs in Docker. The container is defined in `../docker-compose.yml`.

Connection details:

| Setting | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `soldaten_wohnen` |
| Username | `soldaten_wohnen` |
| Password | `soldaten_wohnen` |

Spring Boot reads these values from `src/main/resources/application.properties`.

### What Flyway Does

Flyway owns the database schema. The `@Table("wohnungen")` annotation in `WohnungRepositoryAdapter.kt` does not create a table. It only tells Spring Data JDBC which table a Kotlin class maps to.

The table is created by SQL migrations in:

```text
src/main/resources/db/migration
```

Current migrations:

| File | Purpose |
|---|---|
| `V1__create_wohnungen.sql` | Creates the `wohnungen` table and index |
| `V2__seed_wohnungen.sql` | Inserts development apartment data |

Spring Boot runs Flyway before the application starts accepting requests. If a migration has not been applied yet, Flyway applies it automatically.

### Migration Naming

Every migration file must use this format:

```text
V{number}__{description}.sql
```

Examples:

```text
V3__add_balkon_to_wohnungen.sql
V4__create_bewerbungen.sql
V5__add_index_on_bewerbungen_status.sql
```

Use two underscores between the version and the description.

### Do Not Edit Applied Migrations

Once a migration has run on any database, do not edit that file. Flyway stores a checksum for applied migrations. If the file changes later, Flyway will refuse to start because it cannot know whether the database still matches the migration history.

If you need to change the schema after a migration was applied, create a new migration.

If you are only experimenting locally and want to start over, reset the database:

```bash
docker compose -f ../docker-compose.yml down -v
docker compose -f ../docker-compose.yml up -d
./mvnw spring-boot:run
```

The `-v` flag removes the Docker volume and deletes all local database data.

## Architecture

The backend uses a simple hexagonal architecture for the apartment catalogue bounded context.

The goal is separation of concerns:

| Layer | Responsibility |
|---|---|
| `domain/model` | Core business concepts, with no Spring or database dependency |
| `application/port/in` | Use case interfaces called by controllers |
| `application/service` | Application orchestration, transactions, and use case implementations |
| `application/port/out` | Interfaces for infrastructure needed by the application |
| `adapter/in` | Inbound adapters such as MVC page controllers and JSON API controllers |
| `adapter/out` | Outbound adapters such as Spring Data JDBC persistence |

Request flow for the catalogue page:

```text
Browser
-> KatalogPageController
-> WohnungenAuflistenUseCase
-> WohnungssucheService
-> WohnungRepository
-> WohnungRepositoryAdapter
-> WohnungJdbcRepository
-> PostgreSQL
```

Dependency direction should point inward. The domain model should not know about Spring MVC, Thymeleaf, JDBC, Flyway, or PostgreSQL.

## Project Structure

```text
backend/
|-- pom.xml
|-- mvnw
|-- README.md
|-- src/
    |-- main/
    |   |-- kotlin/
    |   |   |-- org/soldatenwohnen/
    |   |       |-- SoldatenWohnenApplication.kt
    |   |       |-- SecurityConfig.kt
    |   |       |-- adapter/
    |   |       |   |-- in/
    |   |       |   |   |-- KatalogPageController.kt
    |   |       |   |   |-- WohnungenAuflistenController.kt
    |   |       |   |   |-- WohnungDetailAnzeigenController.kt
    |   |       |   |-- out/
    |   |       |       |-- WohnungRepositoryAdapter.kt
    |   |       |-- core/
    |   |           |-- katalog/
    |   |               |-- domain/model/Wohnung.kt
    |   |               |-- application/port/in/WohnungenAuflistenUseCase.kt
    |   |               |-- application/port/in/WohnungDetailAnzeigenUseCase.kt
    |   |               |-- application/port/out/WohnungRepository.kt
    |   |               |-- application/service/WohnungssucheService.kt
    |   |-- resources/
    |       |-- application.properties
    |       |-- assets/styles.css
    |       |-- static/css/app.css
    |       |-- templates/
    |       |   |-- fragments/site.html
    |       |   |-- wohnungen/index.html
    |       |   |-- wohnungen/detail.html
    |       |   |-- wohnungen/fragments.html
    |       |-- db/migration/
    |           |-- V1__create_wohnungen.sql
    |           |-- V2__seed_wohnungen.sql
    |-- test/
        |-- kotlin/org/soldatenwohnen/SoldatenWohnenApplicationTests.kt
```

### Important Files

| File | Why it matters |
|---|---|
| `pom.xml` | Maven dependencies and Kotlin compiler setup |
| `application.properties` | App name, datasource, Flyway, security user, server port |
| `SecurityConfig.kt` | Public page routes and protected API routes |
| `KatalogPageController.kt` | Thymeleaf page routes and HTMX fragment routes |
| `WohnungenAuflistenController.kt` | JSON API list endpoint |
| `WohnungDetailAnzeigenController.kt` | JSON API detail endpoint |
| `WohnungRepositoryAdapter.kt` | Spring Data JDBC row mapping and repository adapter |
| `Wohnung.kt` | Domain model for apartments |
| `templates/wohnungen/*.html` | Catalogue page, detail page, and reusable fragments |
| `assets/styles.css` | Tailwind v4 source file |
| `static/css/app.css` | Generated CSS served by Spring Boot |
| `db/migration/*.sql` | Flyway schema migrations |

## Common Tasks

### Add A New HTML Page

1. Add a Thymeleaf template under `src/main/resources/templates`.
2. Add a method to a controller in `adapter/in`.
3. Return the template name from the controller method.
4. If you used new Tailwind classes, rebuild `static/css/app.css`.
5. Run `./mvnw test`.

### Add An HTMX Interaction

1. Add `hx-get`, `hx-post`, or another HTMX attribute to a template.
2. Add a controller method that returns a Thymeleaf fragment, not a whole page.
3. Put the fragment in a template such as `wohnungen/fragments.html`.
4. Keep the response HTML small and focused on the element being swapped.
5. Run `./mvnw test`.

### Add A New Column To `wohnungen`

1. Create a new migration in `src/main/resources/db/migration`, for example `V3__add_balkon_to_wohnungen.sql`.
2. Add the column with SQL in that migration.
3. Add the corresponding property to `WohnungRow`.
4. Update `toDomain()` if the domain model needs the new data.
5. Update templates or API response DTOs if the new data should be displayed.
6. Start the app so Flyway applies the migration.
7. Run `./mvnw test`.

### Add A New Use Case

1. Add an inbound port under `core/katalog/application/port/in`.
2. Implement it in an application service under `core/katalog/application/service`.
3. Add or reuse an outbound port under `core/katalog/application/port/out` if persistence is needed.
4. Implement the outbound port in `adapter/out`.
5. Add an inbound adapter in `adapter/in`, usually a page controller or API controller.
6. Run `./mvnw test`.

## Troubleshooting

### `Connection to localhost:5432 refused`

PostgreSQL is not running or Docker is not available. Start the database:

```bash
docker compose -f ../docker-compose.yml up -d
```

Then rerun the app or tests.

### Flyway Checksum Error

A migration file that already ran was edited. The normal fix is to undo the edit and create a new migration with the next version number.

For local throwaway data only, you can reset the database:

```bash
docker compose -f ../docker-compose.yml down -v
docker compose -f ../docker-compose.yml up -d
```

### API Returns Login Page Or `401`

The HTML catalogue routes are public, but API routes are protected. Use HTTP Basic auth during development:

```bash
curl -u biren:biren http://localhost:8080/api/katalog/wohnungen
```

### CSS Changes Do Not Show Up

Rebuild Tailwind CSS:

```bash
pnpm dlx @tailwindcss/cli@4.3.0 -i ./src/main/resources/assets/styles.css -o ./src/main/resources/static/css/app.css --minify
```

Restart Spring Boot if your browser still receives the old static file.
