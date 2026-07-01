# Soldaten Wohnen Backend

The backend application for Soldaten Wohnen. It serves both the backend API and the server-rendered HTML user interface.

There is no separate JavaScript frontend project. Pages are rendered with **Thymeleaf**, small interactive updates use **HTMX**, client-side interactivity uses **Alpine.js**, styling is generated with **Tailwind CSS v4** and **DaisyUI 5**, and data is stored in **PostgreSQL**.

**Important:** This project does not contain a JavaScript frontend, a TypeScript codebase, a bundler like Vite or Webpack, or an SPA framework like React or Vue. Two small JavaScript libraries do run in the browser — **HTMX** and **Alpine.js** — but they are loaded as minified files from WebJars, not built or bundled from source. The `package.json` exists for two reasons: **IntelliJ IDEA requires a locally installed `tailwindcss` npm package to provide Tailwind CSS class autocompletion in Thymeleaf templates**, and **DaisyUI 5** is installed as an npm package and used as a Tailwind CSS plugin. No Node.js code runs in the browser.

## Contents

1. [Mental Model: How Everything Fits Together](#mental-model-how-everything-fits-together)
2. [Current Stack](#current-stack)
3. [One-Time Setup](#one-time-setup)
4. [Daily Development Workflow](#daily-development-workflow)
5. [How The CSS Build Works](#how-the-css-build-works)
6. [Application Routes](#application-routes)
7. [Database And Flyway](#database-and-flyway)
8. [Architecture](#architecture)
9. [Project Structure](#project-structure)
10. [Why npm Is Used For Tailwind CSS](#why-npm-is-used-for-tailwind-css)
11. [Updating Dependencies](#updating-dependencies)
12. [Common Tasks](#common-tasks)
13. [Troubleshooting](#troubleshooting)

---

## Mental Model: How Everything Fits Together

### The Big Picture

This is a **server-rendered web application**. The browser never runs an SPA framework or a JavaScript build step. Two small progressive-enhancement libraries — **HTMX** and **Alpine.js** — are loaded from WebJars for interactivity. Otherwise:

1. The user clicks a link or submits a form.
2. The browser sends a normal HTTP request to the Spring Boot server.
3. Spring Boot runs Kotlin code, queries PostgreSQL, and decides what HTML to return.
4. **Thymeleaf** renders the HTML template on the server.
5. The browser receives raw HTML and displays it.
6. For small interactive updates (like filtering a list), **HTMX** intercepts the click, sends a background request, and swaps a fragment of the page without a full reload.
7. For client-side state that doesn't need the server (like toggling a modal or a dropdown), **Alpine.js** handles it directly in the browser.
8. **Tailwind CSS** together with **DaisyUI 5** provides the styling. The result is a static CSS file served by Spring Boot like any other asset.

### Why npm Is Present In This Project

Tailwind CSS v4 can be installed and run in two ways:

1. **Standalone CLI** — a single binary, no npm required. Fast and self-contained.
2. **npm package** — installed via `npm install`. Slightly more overhead, but universally supported by IDE tooling.

**This project uses the npm package.** The reason is simple: IntelliJ IDEA's Tailwind CSS Language Server detects the `tailwindcss` package in `node_modules` and uses it to provide class autocompletion, hover documentation, and CSS preview inside Thymeleaf HTML templates. Without the npm package, the language server does not activate, and you get no autocomplete.

This is the **only** reason `package.json` and `node_modules` exist. They are not used for a JavaScript build pipeline, a frontend framework, or client-side bundling.

### How Hot Reload Works

There are **two independent hot-reload mechanisms** running in parallel during development:

| Mechanism | Watches | What happens |
|---|---|---|
| **Tailwind CSS `--watch`** | `assets/styles.css`, HTML templates, Kotlin source files | Recompiles `static/css/app.css` whenever you add or remove Tailwind classes |
| **Spring Boot DevTools** | `static/` directory, classpath (Kotlin/Java files) | Restarts the server on code changes; triggers LiveReload in the browser when static assets change |

**The flow when you edit a template:**

1. You add a new Tailwind class to an HTML template.
2. Tailwind's watcher detects the change and rebuilds `app.css` (takes ~40ms).
3. Spring Boot DevTools detects that `app.css` was modified.
4. DevTools either triggers a browser LiveReload, or the next page request simply serves the new file.
5. You see the change immediately without manually refreshing.

**The flow when you edit Kotlin code:**

1. You edit a controller or service.
2. Spring Boot DevTools detects the classpath change.
3. It restarts the embedded server (fast, because only your code changed, not dependencies).
4. The new logic is active on the next request.

### When To Use Thymeleaf, HTMX, and Alpine.js

The frontend uses three technologies that have some overlapping capabilities. The heuristic for choosing between them is based on **where the source of truth lives**:

| Source of truth | Use |
|---|---|
| Server (auth, permissions, persisted data, server-known state at render time) | **Thymeleaf** |
| Browser interaction after page load (toggles, dropdowns, tabs, transient UI state) | **Alpine.js** |
| Server data that changes *after* initial render (search results, form validation against server state, loading new content) | **HTMX** |

**One-line heuristic:** Thymeleaf decides what's on the page at all (and what's *allowed* to be). Alpine decides how what's already there behaves. HTMX brings in new pieces from the server when the client doesn't have them.

### Security-sensitive rendering: always Thymeleaf

`th:if` runs on the server — the hidden branch is never sent to the browser. It isn't in the HTML. `x-show` / `x-if` runs in the browser — the hidden markup *is* in the HTML, just `display:none`. DevTools reveals and toggles it in two clicks. Admin buttons, sensitive fields, anything `sec:authorize`-gated: **Thymeleaf only**. Alpine is for non-sensitive UI state.

### Resolving the overlaps

**Conditional rendering:**

| Condition depends on… | Use |
|---|---|
| Auth / permissions / server-known state at render time | Thymeleaf `th:if` |
| User interaction after page load (click, toggle, input) | Alpine `x-show` / `x-if` |
| Server state that *changes after* load | HTMX re-fetch + Thymeleaf fragment |

**"While typing" form validation — layer them in this order:**

1. **HTML5** (`required`, `type="email"`, `pattern`, `minlength`) — free, native, first line.
2. **Alpine.js** — instant feedback for rules the client knows (length counter, password match, regex). No network.
3. **HTMX** (`hx-trigger="keyup changed delay:400ms"`) — for rules only the server can answer (username available, cross-record rules). Returns an error fragment.
4. **Spring `@Valid` + Thymeleaf** — authoritative fallback that works without JS. Renders errors after a full submit.

Heuristic: "valid email format" → Alpine/HTML5. "Username taken?" → HTMX. Always keep tier 4 regardless — tiers 1–3 are progressive enhancement.

### They cooperate, they don't just divide

One element can legitimately use all three — Thymeleaf decides *if* it's rendered (security), Alpine manages *open/close state*, HTMX *loads its content* from the server when opened:

```html
<button th:if="${#authorization.expression('hasRole(''ADMIN'')')}"
        x-data="{ open: false }" @click="open = true"
        class="btn btn-primary">Edit</button>

<dialog class="modal" :class="open && 'modal-open'">
  <div class="modal-box" hx-get="/wohnungen/42/edit" hx-target="this" hx-swap="innerHTML">
    <!-- HTMX fills this with a Thymeleaf-rendered form when the modal opens -->
  </div>
</dialog>
```

The event bridge runs both ways: HTMX fires `htmx:afterSwap` that Alpine can listen for (re-initialise a widget on new content), and Alpine can call `htmx.trigger(el, 'refresh')` to fire an HTMX request from Alpine state.

---

## Current Stack

| Concern | Technology | Where it lives |
|---|---|---|
| JVM language | Kotlin | `src/main/kotlin` |
| Web framework | Spring Boot Web MVC | Controllers in `adapter/in` |
| HTML rendering | Thymeleaf | `src/main/resources/templates` |
| HTML fragment updates | HTMX | Served from a WebJar under `/webjars/**` |
| Client-side interactivity | Alpine.js | Served from a WebJar under `/webjars/**` |
| Styling: utilities | Tailwind CSS v4 (via npm) | Source in `assets/styles.css`, output in `static/css/app.css` |
| Styling: components | DaisyUI 5 (via npm) | Configured as a Tailwind plugin in `assets/styles.css` |
| CSS build tool | `@tailwindcss/cli` (via npm) | Scripts in `package.json` |
| Database access | Spring Data JDBC | `adapter/out/WohnungRepositoryAdapter.kt` |
| Database | PostgreSQL | Docker Compose at `docker-compose.yml` |
| Schema migrations | Flyway | `src/main/resources/db/migration` |
| Build tool | Maven Wrapper | `./mvnw` |
| Dev hot reload | Spring Boot DevTools + Tailwind `--watch` | See `bin/dev.sh` |

---

## One-Time Setup

These steps are needed once per development machine.

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Install Frontend Dependencies

```bash
npm install
```

This installs `tailwindcss`, `@tailwindcss/cli`, and `daisyui` into `node_modules/`. It is ignored by git. The `tailwindcss` package is what IntelliJ detects for autocompletion. The `@tailwindcss/cli` package provides the `tailwindcss` command used by the build scripts. The `daisyui` package is the component library used as a Tailwind CSS plugin.

You need **Node.js and npm** installed on your machine for this step. You do not need to know anything about npm beyond running `npm install` and the occasional `npm update`.

### 3. Build The CSS For The First Time

```bash
./bin/build-css.sh
```

This compiles `assets/styles.css` into `static/css/app.css`. You only need to run this manually once; during development the watcher handles it, and during production builds Maven handles it automatically.

---

## Daily Development Workflow

### 1. Start The Database

```bash
docker compose up -d
```

Data persists in a Docker volume, so stopping and restarting the container does not wipe the database.

### 2. Start The Development Server

```bash
./bin/dev.sh
```

This script does two things simultaneously:

1. Starts **Tailwind CSS in `--watch` mode** in the background. It watches `assets/styles.css` and all HTML/Kotlin templates, and rebuilds `static/css/app.css` on every change.
2. Starts **Spring Boot** in the foreground. DevTools is active. LiveReload runs on port `35729`.

Press `Ctrl+C` to stop both processes cleanly.

### 3. Open The Application

```text
http://localhost:8080/wohnungen
```

The home route `/` redirects to the catalogue page.

### 4. Make Changes And See Them Instantly

- **Edit HTML templates**: Save the file. Tailwind rebuilds CSS if you added new classes. The browser may auto-reload via LiveReload.
- **Edit Kotlin code**: Save the file. Spring Boot DevTools restarts the server in ~1-2 seconds.
- **Edit `assets/styles.css`**: Save the file. Tailwind rebuilds immediately.

You do **not** need to stop and restart `./bin/dev.sh` for normal template or code changes.

### 5. Run Tests

```bash
./mvnw test
```

Tests start the full Spring application context, so PostgreSQL must be running.

For a fast compile-only check:

```bash
./mvnw -DskipTests compile
```

### 6. Build For Production

```bash
./mvnw clean package
```

Maven automatically runs `./bin/build-css.sh` during the `generate-resources` phase, so the packaged JAR always contains the latest compiled CSS. No manual CSS build step is required.

---

## How The CSS Build Works

### Source File

`src/main/resources/assets/styles.css` is the **source** file. It is a valid CSS file that uses Tailwind v4 directives plus the DaisyUI plugin:

```css
@import "tailwindcss";
@plugin "daisyui" {
  themes: false;
}
@plugin "daisyui/theme" {
  name: "sowo";
  default: true;
  prefersdark: true;
  color-scheme: dark;

  --color-base-100: oklch(20% 0.02 255);
  /* ...all theme color tokens... */

  --radius-selector: 0.5rem;
  --radius-field: 0.375rem;
  --radius-box: 0.75rem;
  /* ...size, border, depth, noise... */
}

@source "../templates/**/*.html";
@source "../../kotlin/**/*.kt";

@theme {
  --font-sans: Inter, ui-sans-serif, system-ui, sans-serif;
}

@layer base { ... }
@layer components { ... }
```

- `@import "tailwindcss"` brings in the entire Tailwind framework.
- `@plugin "daisyui"` registers DaisyUI 5 as a Tailwind plugin. `themes: false` disables all 35 built-in themes so they don't leak into the output.
- `@plugin "daisyui/theme"` defines a custom theme called `sowo`. Every color, radius, and size token is set here. This is how the site avoids looking like a generic DaisyUI site — all 35 built-in themes are disabled and only `sowo` ships.
- `@source` tells Tailwind which files to scan for class names. This is how Tailwind knows which utility *and* DaisyUI component classes to include in the output.
- `@theme` defines custom Tailwind design tokens (fonts, spacing) using CSS custom properties.
- `@layer base` and `@layer components` are for your own custom CSS.

**No `tailwind.config.js` or `postcss.config.js` is needed.** Tailwind v4 is fully configured inside the CSS file itself.

### Styling Philosophy: DaisyUI vs Tailwind

**DaisyUI is the bread and butter. Tailwind is the fine-tuning.**

- Use **DaisyUI component classes** (`btn`, `card`, `badge`, `table`, `modal`, `tabs`, `menu`, `drawer`, `alert`, `input`, `select`, etc.) for the vast majority of the UI. They are semantic, terse, and consistent across all 50 screens. Restyle them globally by editing the `sowo` theme tokens in `styles.css` — one change updates every instance.
- Use **Tailwind utility classes** for layout, spacing, and one-off customisation that DaisyUI's component classes weren't designed for (e.g. a bespoke marketing hero with asymmetric layout). Add utilities alongside DaisyUI classes: `class="btn btn-primary rounded-none"`.

The `sowo` theme is the single point of customisation for the site's visual identity. Change `--color-primary`, `--radius-box`, `--depth`, etc. and every DaisyUI component updates. Do not override component styles piecemeal across templates — that defeats the purpose of having a theme.

### JIT Pruning

DaisyUI component classes (`.btn`, `.card`, `.badge`, etc.) are only emitted into `app.css` **if they appear in a scanned template**. Tailwind v4's JIT engine scans the `@source` files and prunes everything unused. This means the CSS bundle grows only with what you actually use — typically 6–10 KB gzipped for a moderate set of components, compared to shipping the entire library.

### Compiled File

`src/main/resources/static/css/app.css` is the **compiled** file. It is a self-contained, minified CSS file that includes only the utility classes actually used in your templates. It is served by Spring Boot as a static asset.

### npm Scripts

The `package.json` defines three scripts:

| Script | Command | When to use |
|---|---|---|
| `build` | `tailwindcss -i ... -o ...` | Development build without minification |
| `build-prod` | `tailwindcss -i ... -o ... --minify` | Production build. Used by Maven and `build-css.sh`. |
| `watch` | `tailwindcss -i ... -o ... --watch` | Development. Used by `dev.sh`. Rebuilds on every file change. |

You can run these directly if you prefer:

```bash
npm run build-prod
npm run watch
```

### Build Scripts

| Script | When to use |
|---|---|
| `./bin/build-css.sh` | One-off build. Used by Maven during `./mvnw package`. |
| `./bin/dev.sh` | Development. Runs Tailwind watcher + Spring Boot together. |

### What Happens If You Delete `app.css`

Nothing bad. Run `./bin/build-css.sh` and it will be recreated. If you build the project with Maven, it will also be recreated automatically.

### What Happens If You Delete `node_modules`

Run `npm install` again. Both `build-css.sh` and `dev.sh` will automatically run `npm install` for you if `node_modules` is missing, but doing it manually during one-time setup avoids the delay during your first build.

---

## Application Routes

### Public HTML Routes

Rendered with Thymeleaf and accessible without authentication:

| Route | Purpose |
|---|---|
| `GET /` | Redirects to catalogue page |
| `GET /wohnungen` | Catalogue page |
| `GET /wohnungen/suche?q=...` | HTMX fragment for the filtered apartment list |
| `GET /wohnungen/{id}` | Apartment detail page |
| `GET /wohnungen/{id}/kontakt` | HTMX fragment for contact details |
| `GET /css/app.css` | Generated Tailwind stylesheet |
| `GET /webjars/**` | WebJar assets, including HTMX |

### API Routes

JSON API routes, protected by Spring Security:

| Route | Purpose |
|---|---|
| `GET /api/katalog/wohnungen` | Published apartment list as JSON |
| `GET /api/katalog/wohnungen/{id}` | Apartment detail as JSON |

Development credentials (from `application.properties`):

```text
Username: biren
Password: biren
```

Example API call:

```bash
curl -u biren:biren http://localhost:8080/api/katalog/wohnungen
```

### Actuator Endpoints

Spring Boot Actuator is enabled. All endpoints are exposed:

```text
http://localhost:8080/actuator
```

Useful during development for monitoring and debugging.

---

## Database And Flyway

### Local Database

PostgreSQL runs in Docker. Defined in `docker-compose.yml`.

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

The actual table is created by SQL migrations in:

```text
src/main/resources/db/migration
```

Current migrations:

| File | Purpose |
|---|---|
| `V1__create_wohnungen.sql` | Creates the `wohnungen` table and index |
| `V2__seed_wohnungen.sql` | Inserts development apartment data |

Spring Boot runs Flyway before accepting requests. If a migration has not been applied, Flyway applies it automatically.

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

Once a migration has run on any database, do not edit that file. Flyway stores a checksum for applied migrations. If the file changes later, Flyway will refuse to start.

If you need to change the schema after a migration was applied, create a new migration.

If you are only experimenting locally and want to start over, reset the database:

```bash
docker compose down -v
docker compose up -d
./bin/dev.sh
```

The `-v` flag removes the Docker volume and deletes all local database data.

---

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

Dependency direction points inward. The domain model does not know about Spring MVC, Thymeleaf, JDBC, Flyway, or PostgreSQL.

---

## Project Structure

```text
soldaten-wohnen/
|-- pom.xml                              # Maven dependencies and build configuration
|-- mvnw                                 # Maven wrapper script
|-- package.json                         # Tailwind CSS npm dependencies and build scripts
|-- bin/
|   |-- build-css.sh                    # One-off CSS build (used by Maven and manual runs)
|   |-- dev.sh                          # Development: runs Tailwind watcher + Spring Boot
|-- node_modules/                        # Installed npm packages (ignored by git)
|-- src/
|   |-- main/
|   |   |-- kotlin/
|   |   |   |-- org/soldatenwohnen/
|   |   |       |-- SoldatenWohnenApplication.kt
|   |   |       |-- SecurityConfig.kt
|   |   |       |-- adapter/
|   |   |       |   |-- in/
|   |   |       |   |   |-- KatalogPageController.kt
|   |   |       |   |   |-- WohnungenAuflistenController.kt
|   |   |       |   |   |-- WohnungDetailAnzeigenController.kt
|   |   |       |   |-- out/
|   |   |       |       |-- WohnungRepositoryAdapter.kt
|   |   |       |-- core/
|   |   |           |-- katalog/
|   |   |               |-- domain/model/Wohnung.kt
|   |   |               |-- application/port/in/WohnungenAuflistenUseCase.kt
|   |   |               |-- application/port/in/WohnungDetailAnzeigenUseCase.kt
|   |   |               |-- application/port/out/WohnungRepository.kt
|   |   |               |-- application/service/WohnungssucheService.kt
|   |   |-- resources/
|   |       |-- application.properties   # App config, datasource, security, Thymeleaf, DevTools
|   |       |-- assets/
|   |       |   |-- styles.css          # Tailwind v4 source file
|   |       |-- static/
|   |       |   |-- css/
|   |       |       |-- app.css         # Generated CSS served by Spring Boot
|   |       |-- templates/
|   |       |   |-- fragments/htmx_ajs.html    # Shared <head> with HTMX + Alpine.js WebJars
|   |       |   |-- katalog/index.html          # Landing/catalogue page
|   |       |   |-- katalog/fragments/heroSection.html
|   |       |   |-- katalog/fragments/featureSection.html
|   |       |   |-- katalog/hx-fragments/page-update.html
|   |       |-- db/migration/
|   |           |-- V1__create_wohnungen.sql
|   |           |-- V2__seed_wohnungen.sql
|   |-- test/
|       |-- kotlin/org/soldatenwohnen/SoldatenWohnenApplicationTests.kt
```

### Important Files

| File | Why it matters |
|---|---|
| `pom.xml` | Maven dependencies, Kotlin compiler, exec plugin for CSS build |
| `package.json` | Tailwind CSS + DaisyUI versions and npm build scripts (`build`, `build-prod`, `watch`) |
| `application.properties` | App name, datasource, Flyway, security, Thymeleaf, DevTools |
| `SecurityConfig.kt` | Public page routes and protected API routes |
| `KatalogPageController.kt` | Thymeleaf page routes and HTMX fragment routes |
| `WohnungenAuflistenController.kt` | JSON API list endpoint |
| `WohnungDetailAnzeigenController.kt` | JSON API detail endpoint |
| `WohnungRepositoryAdapter.kt` | Spring Data JDBC row mapping and repository adapter |
| `Wohnung.kt` | Domain model for apartments |
| `templates/katalog/*.html` | Catalogue page and reusable fragments |
| `assets/styles.css` | Tailwind v4 + DaisyUI 5 source file (plugin, `sowo` theme, custom layers) |
| `static/css/app.css` | Generated CSS served by Spring Boot |
| `db/migration/*.sql` | Flyway schema migrations |

---

## Why npm Is Used For Tailwind CSS

This section explains the design decision and the trade-offs.

### The Two Ways To Install Tailwind CSS v4

Tailwind CSS v4 can be obtained in two forms:

1. **Standalone CLI** — a single ~80MB executable downloaded from GitHub releases. No npm, no `node_modules`, no `package.json`.
2. **npm package** — installed via `npm install tailwindcss` and `npm install @tailwindcss/cli`.

### What We Tried First

We initially used the **standalone CLI**. The build worked perfectly. CSS compiled in 40ms. No npm was involved. Maven builds were clean. The `dev.sh` script ran the watcher without any JavaScript tooling.

### Why We Switched To npm

**IntelliJ IDEA does not support the standalone CLI for autocompletion.** Its bundled Tailwind CSS Language Server is designed around the npm ecosystem. It looks for `node_modules/tailwindcss` to detect a Tailwind project. Without that package, the language server never activates, and you get **no class autocompletion** inside Thymeleaf HTML templates.

JetBrains documentation recommends installing `@tailwindcss/language-server` globally and pointing IntelliJ to it, but in practice this does not work reliably for Tailwind v4 projects without a local `node_modules/tailwindcss` installation.

The `package.json` and `node_modules` exist **only** to satisfy the IDE. They are not part of the application's runtime.

### Advantages Of The npm Route

| Advantage | Explanation |
|---|---|
| **IDE autocompletion works** | IntelliJ provides Tailwind class completion, hover documentation, and CSS preview in HTML templates. |
| **Standard ecosystem** | The `build`, `build-prod`, `watch` scripts in `package.json` match the setup described in *Taming Thymeleaf* and other learning resources. |
| **Easy plugin additions** | Tailwind plugins like DaisyUI can be added to `package.json` and registered with `@plugin` in `styles.css`. |
| **Explicit version management** | `package.json` records the exact Tailwind version. Upgrading is a single `npm update` command. |

### Disadvantages Of The npm Route

| Disadvantage | Explanation |
|---|---|
| **Requires Node.js and npm** | You must have Node.js installed on your development machine. The standalone CLI had no runtime dependencies. |
| **`node_modules` disk usage** | `node_modules` takes ~50-100MB of disk space and is ignored by git. |
| **Slightly more complex mental model** | You must understand that npm is used for the CSS compiler, not for a JavaScript frontend. |
| **Slower initial setup** | `npm install` downloads many small files. The standalone CLI was one 80MB download. |
| **One more tool to update** | You now need to keep both Maven dependencies and npm dependencies up to date. |

### The Bottom Line

The npm route is a **pragmatic compromise**. It introduces a small amount of JavaScript tooling into an otherwise build-pipeline-free project, but the payoff is a significantly better development experience inside IntelliJ IDEA and access to the Tailwind plugin ecosystem (DaisyUI). The browser receives plain HTML and CSS plus two small progressive-enhancement libraries (HTMX, Alpine.js) loaded from WebJars. No SPA framework or JavaScript build step is involved.

---

## Updating Dependencies

### Update Tailwind CSS

Tailwind releases new versions regularly. To check for updates:

```bash
npm outdated
```

To update to the latest version:

```bash
npm update tailwindcss @tailwindcss/cli
```

Then rebuild the CSS and verify the application still looks correct:

```bash
./bin/build-css.sh
```

If everything is fine, commit the updated `app.css` and the updated `package.json`. `node_modules` is never committed.

To see exactly what changed, check the Tailwind releases page:

```text
https://github.com/tailwindlabs/tailwindcss/releases
```

### Update DaisyUI

DaisyUI is updated similarly:

```bash
npm update daisyui
```

Then rebuild the CSS and verify the application still looks correct:

```bash
./bin/build-css.sh
```

Check the DaisyUI changelog for breaking changes:

```text
https://daisyui.com/docs/changelog/
```

### Update Spring Boot And Kotlin

Spring Boot and Kotlin versions are declared in `pom.xml`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>
```

```xml
<properties>
    <java.version>24</java.version>
    <kotlin.version>2.4.0-RC2</kotlin.version>
</properties>
```

To check for new Spring Boot versions:

1. Visit the Spring Boot releases page: `https://github.com/spring-projects/spring-boot/releases`
2. Look for the latest stable version in the same major version line (e.g., `4.0.x` or `4.1.x`).
3. Update the `<version>` tag in `pom.xml`.
4. Run `./mvnw clean compile` and fix any deprecation warnings.
5. Run `./mvnw test` to verify everything still works.

To check for new Kotlin versions:

1. Visit `https://github.com/JetBrains/kotlin/releases`
2. Update the `<kotlin.version>` property in `pom.xml`.
3. Rebuild and test as above.

### Update Other Dependencies

Some dependencies have explicit versions in `pom.xml`:

| Dependency | Current Version | Where to check for updates |
|---|---|---|
| `springdoc-openapi-starter-webmvc-ui` | `3.0.3` | `https://springdoc.org/` or Maven Central |
| `htmx-spring-boot` | `5.1.0` | `https://github.com/wimdeblauwe/htmx-spring-boot/releases` |
| `htmx.org` (WebJar) | `2.0.6` | `https://github.com/bigskysoftware/htmx/releases` |

For dependencies managed by the Spring Boot parent (most of them), updating the parent version is usually enough. For dependencies with explicit versions, check their release notes and update the `<version>` tag in `pom.xml`.

After any dependency change, run:

```bash
./mvnw clean compile
./mvnw test
```

---

## Common Tasks

### Add A New HTML Page

1. Add a Thymeleaf template under `src/main/resources/templates`.
2. Set `data-theme="sowo"` on the `<html>` tag (or include a shared layout fragment that does this).
3. Add a method to a controller in `adapter/in`.
4. Return the template name from the controller method.
5. If you used new DaisyUI or Tailwind classes, save the template. The Tailwind watcher (if running) or `./bin/build-css.sh` will update `app.css`.
6. Run `./mvnw test`.

### Add An HTMX Interaction

1. Add `hx-get`, `hx-post`, or another HTMX attribute to a template.
2. Add a controller method that returns a Thymeleaf fragment, not a whole page.
3. Put the fragment in a template such as `katalog/hx-fragments/`.
4. Keep the response HTML small and focused on the element being swapped.
5. Run `./mvnw test`.

### Add An Alpine.js Interaction

1. Add `x-data` to the element that needs local state (e.g. `x-data="{ open: false }"`).
2. Use `x-show`, `@click`, `x-bind`, etc. for the interaction. No controller method is needed — Alpine runs entirely in the browser.
3. Use Alpine for non-sensitive UI state only (toggles, tabs, dropdowns). For security-gated rendering, use Thymeleaf `th:if` instead.
4. Run `./mvnw test`.

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

### Rebuild CSS Manually

If the Tailwind watcher is not running and you need to update `app.css`:

```bash
./bin/build-css.sh
```

Or use the npm script directly:

```bash
npm run build-prod
```

This is also run automatically during `./mvnw compile` and `./mvnw package`.

---

## Troubleshooting

### `Connection to localhost:5432 refused`

PostgreSQL is not running or Docker is not available. Start the database:

```bash
docker compose up -d
```

Then rerun the app or tests.

### Flyway Checksum Error

A migration file that already ran was edited. The normal fix is to undo the edit and create a new migration with the next version number.

For local throwaway data only, reset the database:

```bash
docker compose down -v
docker compose up -d
```

### API Returns Login Page Or `401`

The HTML catalogue routes are public, but API routes are protected. Use HTTP Basic auth during development:

```bash
curl -u biren:biren http://localhost:8080/api/katalog/wohnungen
```

### CSS Changes Do Not Show Up

1. Check that the Tailwind watcher is running (it is started by `./bin/dev.sh`).
2. If you are not using `./bin/dev.sh`, run `./bin/build-css.sh` manually.
3. Refresh the browser. DevTools LiveReload may not trigger if the watcher and Spring Boot are not running together.
4. Check `static/css/app.css` in your editor to confirm the file was updated.

### `./bin/dev.sh` Or `./bin/build-css.sh` Says `npm: command not found`

Node.js and npm are not installed or not in your PATH. Install them:

- **macOS**: `brew install node`
- **Ubuntu/Debian**: `sudo apt install nodejs npm`
- **Windows**: Download from `https://nodejs.org/`

### `./bin/dev.sh` Says `node_modules is missing` And Hangs

The first time you run `./bin/dev.sh` or `./bin/build-css.sh` after cloning the repository, the scripts will automatically run `npm install` if `node_modules` is missing. This can take 10-30 seconds depending on your internet connection. Wait for it to finish. On subsequent runs it will be instant.

To avoid the delay, run `npm install` manually during one-time setup:

```bash
npm install
```

### `pollInterval must be greater than QuietPeriod` Error

This was a DevTools configuration error where `spring.devtools.restart.poll-interval` was set equal to `spring.devtools.restart.quiet-period`. It has been fixed in `application.properties` (poll is now `2s`, quiet is `1s`). If you see this error, ensure `poll-interval` is strictly greater than `quiet-period`.

### Browser Shows `404` For `/css/app.css`

The compiled CSS file is missing. Rebuild it:

```bash
./bin/build-css.sh
```

Or start the app with `./bin/dev.sh`, which rebuilds CSS automatically.

### `./bin/dev.sh` Does Not Stop Cleanly

If you see zombie processes after pressing `Ctrl+C`:

```bash
# Find the Tailwind watcher
pgrep -f "tailwindcss.*--watch"
# Kill it
pkill -f "tailwindcss.*--watch"
```

### IntelliJ Does Not Show Tailwind Autocompletion

1. Make sure `npm install` has been run and `node_modules/tailwindcss` exists.
2. Open **Settings → Languages & Frameworks → Style Sheets → Tailwind CSS**.
3. Make sure the **Language Server** path is set to the bundled server or a globally installed `@tailwindcss/language-server`.
4. Look at the **bottom status bar** in IntelliJ for the language services widget. Click it and restart the Tailwind CSS language server.
5. Open a Thymeleaf HTML template and wait a few seconds for the server to initialize.

If it still does not work, the issue is likely on the JetBrains side. The `tailwindcss` npm package in `node_modules` is the standard detection signal. As a last resort, try **File → Invalidate Caches → Invalidate and Restart**.

