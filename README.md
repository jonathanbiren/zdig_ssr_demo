# Soldaten Wohnen

Spring Boot serves the application UI with Thymeleaf templates. HTMX adds small server-driven interactions, and Tailwind CSS v4 is built into Spring Boot's static resources.

## Development

Start PostgreSQL from the repository root:

```bash
docker compose up -d
```

The UI is backend-only. If you change template classes or `backend/src/main/resources/assets/styles.css`, rebuild Tailwind CSS from `backend/`:

```bash
pnpm dlx @tailwindcss/cli@4.3.0 -i ./src/main/resources/assets/styles.css -o ./src/main/resources/static/css/app.css --minify
```

Run the application from `backend/`:

```bash
./mvnw spring-boot:run
```

Open `http://localhost:8080/wohnungen`.

## Frontend Strategy

The old Solid/TanStack/Vite SPA and the `frontend/` directory have been removed:

- `backend/src/main/resources/assets/styles.css` imports Tailwind and points Tailwind at the Thymeleaf templates.
- The generated stylesheet is committed at `backend/src/main/resources/static/css/app.css`.
- Spring Boot serves HTML, CSS, and the HTMX WebJar from the same origin.
- Application routes live in Spring MVC controllers, not browser-side route files.

## Backend Routes

- `GET /` renders the apartment catalogue view.
- `GET /wohnungen` renders the catalogue page.
- `GET /wohnungen/suche?q=...` returns the HTMX-updated listing fragment.
- `GET /wohnungen/{id}` renders the apartment detail page.
- Existing `/api/katalog/wohnungen` JSON endpoints remain available for API use.
