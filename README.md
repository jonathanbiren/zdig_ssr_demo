# Soldaten Wohnen

A server-rendered Kotlin and Spring Boot application for customer (`Kunden`) administration and for viewing customer obligations (`Verpflichtungen`). HTML is rendered on the server with JTE, PostgreSQL is accessed through jOOQ, and HTMX and Alpine.js provide progressive enhancement in the browser.

There is no SPA, TypeScript application, React/Vue frontend, or JavaScript bundler. Node.js is used only for Tailwind CSS tooling and IDE support.

## Contents

1. [Architecture](#architecture)
2. [Technology Stack](#technology-stack)
3. [Prerequisites and Setup](#prerequisites-and-setup)
4. [Daily Development](#daily-development)
5. [Maven, Generated Code, and IntelliJ](#maven-generated-code-and-intellij)
6. [Templates and Browser Interaction](#templates-and-browser-interaction)
7. [Database, Flyway, and jOOQ](#database-flyway-and-jooq)
8. [CSS and npm](#css-and-npm)
9. [Project Structure](#project-structure)
10. [Common Workflows](#common-workflows)
11. [Testing and Verification](#testing-and-verification)
12. [Troubleshooting](#troubleshooting)

---

## Architecture

### Direction: functional core, imperative edge

The project is moving toward the functional approach to domain-driven design described in Scott Wlaschin's *Domain Modeling Made Functional*, together with a **functional core and imperative edge/shell**.

This is an architectural direction, not a claim that every existing class is already purely functional. The rules below describe how new code should be written and how existing code should be simplified over time.

### Functional core

Code under `org.zdigdemo.core` should contain the business model and business rules:

- Immutable domain types and value objects.
- Validating factory functions that make invalid domain objects difficult or impossible to construct.
- Explicit validation results, using Arrow `Either` where useful, instead of exceptions for expected input errors.
- Pure transformations and decisions: no HTTP, database, file system, clock, random ID generation, Spring, or JTE dependencies.
- No jOOQ records or generated persistence types in the core model.

The core should be testable with ordinary Kotlin unit tests, without starting Spring or PostgreSQL. The existing `Kunde.create` factory is the concrete model: it validates untrusted customer data and returns either accumulated validation errors or a valid `Kunde`.

### Imperative edge

Code under `org.zdigdemo.edge` deals with the outside world:

- Controllers accept HTTP requests and construct HTTP/HTML responses.
- Request DTOs and view models belong at the edge; they are not domain types.
- Controllers convert untrusted input into domain values through core validation functions.
- Repositories execute SQL, transactions, search, and pagination through jOOQ.
- Mapping between domain objects, generated jOOQ records, and presentation models happens at the edge.
- JTE templates render HTML from explicit view models.

A simplified request flow is:

```text
HTTP request
    -> controller / DTO mapping
    -> pure domain validation or decision
    -> repository and PostgreSQL when I/O is required
    -> view model
    -> JTE HTML response
```

Dependencies point toward the core. Core code must not import controllers, repositories, Spring, JTE, or generated jOOQ classes.

### Deliberately little architectural ceremony

The application currently lets controllers call repositories directly. At its present size, that is preferable to creating a service, two ports, and an adapter for every simple CRUD operation.

Do not introduce layers merely to satisfy a diagram. Add an application service when real orchestration appears, and add a port when it provides a genuine decoupling or testing benefit. Functional DDD does not require a repository interface, a use-case interface, or a service class for every domain entity.

---

## Technology Stack

| Concern | Technology |
|---|---|
| Language | Kotlin on Java 25 |
| Web framework | Spring Boot Web MVC |
| Server-side HTML | JTE with Kotlin `.kte` templates |
| HTML fragment updates | HTMX 4, served from a WebJar |
| Transient browser state | Alpine.js, served from a WebJar |
| Database access | jOOQ Kotlin DSL |
| Database | PostgreSQL 17 |
| Migrations | Flyway |
| Functional validation | Arrow Core |
| Styling | Tailwind CSS 4 and DaisyUI 5 |
| Build | Maven Wrapper |
| Development reload | Spring Boot DevTools, JTE development mode, Tailwind watch mode |

The versions in `pom.xml` and `package.json` are authoritative; this document intentionally does not duplicate every version number.

---

## Prerequisites and Setup

Install the following:

- **JDK 25**.
- **Docker** with a running Docker daemon. Docker is required for jOOQ generation and the development PostgreSQL container.
- **Node.js and npm** for Tailwind CSS and DaisyUI.
- IntelliJ IDEA is recommended, but not required. Maven itself does not need to be installed globally; use `./mvnw`.

From the project root:

```bash
# Install Tailwind, DaisyUI, and the class-sorting tooling.
npm install

# Generate jOOQ and JTE sources, build CSS, and compile the application.
./mvnw clean compile

# Start the application and Tailwind watcher.
./bin/dev.sh
```

Open:

```text
http://localhost:8080/
```

Spring Boot's Docker Compose integration is enabled. Starting the application starts `docker-compose.yml` and stops it again according to the configured lifecycle. To manage PostgreSQL independently instead, use:

```bash
docker compose up -d
```

The local database configuration is:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:postgresql://localhost:5432/soldaten_wohnen` |
| Database | `soldaten_wohnen` |
| Username | `soldaten_wohnen` |
| Password | `soldaten_wohnen` |

These are development credentials, not production secrets.

---

## Daily Development

Start Docker, then run:

```bash
./bin/dev.sh
```

The script:

1. Installs npm dependencies if `node_modules` is missing.
2. Starts `npm run watch` to rebuild CSS when relevant files change.
3. Starts Spring Boot with the `dev` profile and DevTools enabled.
4. Stops the Tailwind watcher when the script exits.

### What reloads, and what does not

| Change | Required action |
|---|---|
| Ordinary KTE content | JTE development mode reloads the template; save and refresh |
| Tailwind classes or CSS | The running Tailwind watcher rebuilds `app.css` |
| Kotlin code | Recompile the code; DevTools reacts to classpath changes, not merely to saving a source file |
| Renamed/moved KTE template used through generated `Templates` | Regenerate JTE sources and update Kotlin references |
| Database schema | Add a migration and regenerate jOOQ sources |
| `pom.xml` | Reload the Maven project in IntelliJ and rebuild |

Generated template APIs still matter in development even though JTE development mode can reload template content dynamically.

---

## Maven, Generated Code, and IntelliJ

### What Maven generates

Two groups of Kotlin sources are generated during `generate-sources`:

| Output | Input | Generated by |
|---|---|---|
| `target/generated-sources/jooq` | Real Flyway migrations applied to a temporary PostgreSQL container | jOOQ code generator |
| `target/generated-sources/jte` | `src/main/kte/**/*.kte` | JTE Maven plugin |

CSS is generated later, during `generate-resources`:

| Output | Input | Command used by Maven |
|---|---|---|
| `src/main/resources/static/css/app.css` | `src/main/resources/assets/styles.css` plus scanned KTE/Kotlin files | `npm run build-prod` |

Generated Kotlin code belongs in `target/` and must never be edited. `app.css` is also generated and ignored by Git.

### The important commands

#### Generate jOOQ and JTE sources without compiling

```bash
./mvnw clean generate-sources
```

Use this after:

- Renaming, moving, adding, or deleting KTE templates.
- Changing template parameters.
- Changing the database schema through a migration.
- Removing stale generated sources that no longer match the source tree.

The command stops before compilation. This deliberately allows you to fix broken controller or repository references **after** the new `Templates` and jOOQ APIs exist.

It requires Docker, but does not run the Tailwind build.

#### Generate sources and CSS without compiling

```bash
./mvnw clean generate-resources
```

The phase name is `generate-resources` with a hyphen. This runs the Maven lifecycle through `generate-resources`, including everything from `generate-sources`, and then builds CSS. It still stops before compilation.

Use it when you want all generated artifacts without compiling application code. It requires both Docker and the npm dependencies.

It is **not** a CSS-only command. For CSS alone, prefer:

```bash
./bin/build-css.sh
```

#### Compile and run tests

```bash
./mvnw test
```

This runs generation, CSS compilation, main/test compilation, and tests. Use it before considering a change complete.

#### Perform a full clean build

```bash
./mvnw clean install
```

This runs the complete lifecycle: clean, generate sources, generate CSS, compile, test, package, and install the artifact into the local Maven repository.

Use it for an authoritative full verification or when another local Maven project needs the installed artifact. It is not the right command merely to regenerate sources after a template rename, because known-broken references will make compilation fail. For routine project verification without installing into `~/.m2`, `./mvnw clean verify` is usually sufficient.

A `clean install` that fails during compilation may still have generated the sources successfully—the generation phases happen first—but using `clean generate-sources` makes that intermediate workflow explicit.

### Maven and IntelliJ do different jobs

| Action | What it actually does | When to use it |
|---|---|---|
| `./mvnw clean generate-sources` | Deletes `target`, regenerates jOOQ and JTE Kotlin sources, does not compile | After template or schema API changes |
| `./mvnw clean generate-resources` | Does the above and builds CSS, does not compile | When every generated artifact is needed before fixing compilation |
| `./mvnw clean install` | Full Maven verification and local installation | After source references are fixed, for a complete build |
| **Reload All Maven Projects** in IntelliJ | Reimports Maven configuration, dependencies, and source roots | After `pom.xml` changes or when IntelliJ has stale project metadata |
| **Build → Rebuild Project** in IntelliJ | Recompiles using IntelliJ's current project model and available generated sources | After generation/reload, or to check for stale IDE compilation state |

**Reload Maven Projects does not generate sources. Rebuild Project does not repair missing generated sources. Neither action fixes incorrect Kotlin references.**

### Recommended recovery order

After renaming templates or changing generated jOOQ APIs:

```text
1. ./mvnw clean generate-sources
2. Inspect the newly generated Templates/jOOQ classes.
3. Fix Kotlin references.
4. Run ./mvnw test or ./mvnw clean install.
5. If Maven passes but IntelliJ remains red:
   a. Reload All Maven Projects.
   b. Build -> Rebuild Project.
```

If IntelliJ is stale but Maven already passes, steps 1–4 do not need to be repeated blindly. Reload the Maven project and rebuild the IDE project first. Invalidate caches only as a last resort.

---

## Templates and Browser Interaction

### JTE

Templates live under `src/main/kte`. Customer and validation controllers inject the generated `gg.jte.generated.precompiled.Templates` facade and call its typed methods. The current Personen/Verpflichtungen placeholder controllers instead use Spring view resolution with full names such as `pages/personen/index.kte`; prefer the typed `Templates` API for new application workflows.

Generated method names depend on template paths, and their parameters depend on `@param` declarations. After moving or renaming a template, regenerate sources instead of guessing the new method name.

The checked-in configuration enables JTE development mode and disables precompiled-template lookup. For production, override it with `gg.jte.developmentMode=false` and `gg.jte.usePrecompiledTemplates=true`.

### HTMX

HTMX is used when new server-rendered HTML is needed after the initial page load, for example:

- Searching and sorting the customer table.
- Loading another keyset-pagination page.
- Opening customer details.
- Returning server-side form validation.
- Deleting rows and updating related counters.

HTMX 4 partials can update more than one target from a single response. Keep fragment responses focused on the elements that actually need to change.

### Alpine.js

Alpine is for transient, non-sensitive browser state such as whether a detail panel is open. It should not decide whether a user is authorized to see sensitive content.

A useful division is:

- **JTE:** what the server renders and what the user is allowed to receive.
- **HTMX:** new HTML from the server after an interaction.
- **Alpine:** behavior of HTML already present in the browser.

### Development security

The current security setup is suitable only for local development and demos:

- `/`, `/kunden/**`, and static WebJar/CSS resources are public.
- Other routes, including the Personen/Verpflichtungen placeholders and Actuator, require authentication.
- The default web credentials in `application.properties` are `biren` / `biren`.
- HTTP Basic and form login are enabled; CSRF is disabled globally.

Public `/kunden/**` currently includes state-changing endpoints. Review authorization and CSRF before deploying the application outside a development environment.

---

## Database, Flyway, and jOOQ

Flyway owns the schema. Migrations live in:

```text
src/main/resources/db/migration
```

Use names such as:

```text
V6__add_notiz_to_kunden.sql
```

Do not edit a migration after it has been applied to a database that matters. Add a new migration instead.

### Runtime database

Spring Boot runs Flyway against the configured PostgreSQL database on startup. The local database persists its data in the Docker volume `soldaten_wohnen_data`.

### Build-time database

jOOQ generation does not inspect the development database. During `generate-sources`, Maven:

1. Starts a temporary PostgreSQL 17 container with Testcontainers.
2. Applies the real Flyway migrations to it.
3. Generates typed Kotlin tables, fields, records, and routines into `target/generated-sources/jooq`.
4. Removes the temporary container when the build JVM exits.

Consequently, code generation needs a Docker daemon but not a separately running development database.

Repositories use Spring's configured `DSLContext` and generated jOOQ types. Generated records are persistence-edge types, not core domain models.

---

## CSS and npm

Tailwind's source configuration is:

```text
src/main/resources/assets/styles.css
```

It contains the Tailwind imports, DaisyUI plugin configuration, the `sowo` theme, and the KTE/Kotlin `@source` declarations. No separate Tailwind configuration file is required.

The generated stylesheet is:

```text
src/main/resources/static/css/app.css
```

Useful commands:

```bash
npm run watch              # Rebuild CSS on changes
npm run build-prod         # One-off minified build
./bin/build-css.sh         # One-off build with npm dependency check
npm run sort-classes       # Sort static class attributes in KTE templates
npm run sort-classes:check # Check without modifying files
```

Maven runs `npm run build-prod` directly during `generate-resources`; it does not call `bin/build-css.sh`.

`node_modules`, `app.css`, and generated Maven output are build artifacts and should not be edited or committed.

---

## Project Structure

```text
soldaten-wohnen/
├── pom.xml
├── package.json
├── docker-compose.yml
├── bin/
│   ├── dev.sh
│   └── build-css.sh
├── scripts/
│   └── sort-tailwind-classes.mjs
├── src/
│   ├── main/
│   │   ├── kotlin/org/zdigdemo/
│   │   │   ├── config/                 # Spring and application configuration
│   │   │   ├── core/                   # Pure domain model and business rules
│   │   │   └── edge/
│   │   │       ├── controller/         # HTTP, DTOs, view models, HTML responses
│   │   │       └── repositories/       # jOOQ queries and transactions
│   │   ├── kte/                        # JTE layouts, pages, and components
│   │   └── resources/
│   │       ├── assets/styles.css       # Tailwind source
│   │       ├── db/migration/           # Flyway migrations
│   │       ├── static/css/app.css      # Generated CSS
│   │       └── application.properties
│   └── test/kotlin/
└── target/generated-sources/
    ├── jooq/                           # Generated persistence API
    └── jte/                            # Generated template API
```

Keep this structure as a guide, not as a requirement to create a package for every possible layer.

---

## Common Workflows

### Add or reorganize a JTE page or component

```bash
./mvnw clean generate-sources
```

Then:

1. Inspect the generated `Templates` facade if necessary.
2. Add or update the typed template calls in controllers.
3. Add new Tailwind classes normally; the watcher or CSS build will include them.
4. Run `./mvnw test`.

### Change the database schema

1. Add a new Flyway migration.
2. Run `./mvnw clean generate-sources`.
3. Update repositories and mappings against the regenerated jOOQ API.
4. Run `./mvnw test`.
5. Start the application to apply the migration to the development database.

### Add a business rule

1. Model the rule in `core` using immutable values and a pure function where practical.
2. Return expected validation failures explicitly.
3. Call it from the appropriate edge workflow.
4. Add focused unit tests that do not require Spring or PostgreSQL.

### Add an HTMX interaction

1. Add the HTMX attributes to the relevant KTE element.
2. Add or update an edge controller endpoint.
3. Return only the required JTE fragment or fragments.
4. Verify the target and swap behavior, including empty and error states.

---

## Testing and Verification

```bash
./mvnw test
```

For the full clean verification:

```bash
./mvnw clean verify
```

Or, when a locally installed artifact is specifically needed:

```bash
./mvnw clean install
```

Docker must be running because generation uses Testcontainers. Tests that start the application context may also use the Docker Compose database.

The current suite is small and primarily verifies that the application context loads; it is not yet meaningful feature coverage. Prefer test placement that reflects the architecture:

- Core rules: fast, pure Kotlin unit tests.
- Repositories: PostgreSQL-backed tests for actual SQL, search, and pagination behavior.
- Controllers/HTMX: focused web tests for response HTML and status codes.
- Full application-context tests: use sparingly for wiring verification.

---

## Troubleshooting

### `Templates` methods or generated JTE classes are unresolved after a template rename

Run:

```bash
./mvnw clean generate-sources
```

Then update the Kotlin references to the newly generated methods. If Maven compiles but IntelliJ still shows old names, reload the Maven project and rebuild the IDE project.

### `KUNDEN`, `KundenRecord`, or another generated jOOQ symbol is unresolved

Run `./mvnw clean generate-sources` with Docker running. Check the generation logs for a failed temporary database, Flyway migration, or code generator.

### Maven reports `Could not find a valid Docker environment`

Start Docker Desktop or the Docker daemon and rerun the command. This is required even if the development PostgreSQL container is already running.

### Maven fails during `npm run build-prod`

Check that Node.js and npm are installed, then run:

```bash
npm install
npm run build-prod
```

Fix any error in `src/main/resources/assets/styles.css` before retrying the Maven build.

### Maven succeeds but IntelliJ shows compilation errors

1. Reload All Maven Projects.
2. Build → Rebuild Project.
3. Confirm that the relevant `target/generated-sources` directories exist.
4. Use Invalidate Caches only if the previous steps do not help.

### IntelliJ actions succeed but Maven compilation fails

Treat the Maven compiler errors as real source problems. Reloading Maven metadata or rebuilding the IDE project cannot fix references to renamed templates or changed jOOQ fields. Regenerate the relevant sources and update the code.

### PostgreSQL connection refused on port 5432

Start the development database explicitly if it is not being managed by Spring Boot:

```bash
docker compose up -d
```

### Flyway checksum mismatch

A previously applied migration was edited. Restore it and put the change in a new migration. For disposable local data only, a complete reset is possible:

```bash
docker compose down -v
docker compose up -d
```

**`down -v` permanently deletes the local database volume and all data in it.**

### CSS is missing or stale

```bash
npm install
./bin/build-css.sh
```

During development, use `./bin/dev.sh` so the Tailwind watcher stays running.
