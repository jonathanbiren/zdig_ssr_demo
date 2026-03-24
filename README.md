# Every time you want to work on this project, you need to run: 

#### 1. Start the database (if not already running)
```terminal
docker compose up -d
docker compose down (To tear the container back down) 
```

#### 2. Start the backend (in a terminal, from the backend/ directory)
```terminal
./mvnw spring-boot:run
```
#### Spring Boot starts → Flyway runs any new migrations → app ready on :8080

#### 3. Start the frontend (in another terminal, from the frontend/ directory)
```terminal
pnpm dev
```

#### Vite starts → frontend ready on :3000, proxies /api calls to :8080


# How this monorepo works

Your repository is now two completely independent projects that happen to live under one Git repo. That's it. There is no monorepo tool (no Nx, no Turborepo, no Lerna) coordinating them. The frontend/ and backend/ directories are as unaware of each other as two strangers on a bus. They share a .gitignore and a Git history, and nothing else.
Why they can coexist
The frontend and backend use entirely different ecosystems with zero overlap:

| | Frontend | Backend |
|---|---|---|
| Language | TypeScript | Kotlin |
| Build tool | Vite (invoked via pnpm) | Maven (invoked via ./mvnw) |
| Dependency manifest | package.json | pom.xml |
| Dependency storage | node_modules/ (local to frontend/) | ~/.m2/repository/ (global Maven cache) |
| Dev server | localhost:3000 | localhost:8080 |


There is no conflict because these tools have no awareness of each other. Maven doesn't scan for package.json, and pnpm doesn't look for pom.xml. Each tool only cares about the directory it's run from.
Where dependencies come from
Frontend: When you run `pnpm install inside frontend/`, pnpm reads frontend/package.json, resolves versions using the npm registry, and downloads packages into `frontend/node_modules/`. The `pnpm-lock.yaml` file pins exact versions. Everything is self-contained within that directory.
Backend: When you run `./mvnw compile inside backend/`, Maven reads `backend/pom.xml`, resolves versions from Maven Central (and the Spring Boot parent POM), and caches downloaded JARs in `~/.m2/repository/` on your machine. This is a global cache shared by all Maven projects on your system -- unlike `node_modules`, it doesn't live inside your project.

How to develop each one:


### Frontend -- always cd frontend first:
```
pnpm install    # install deps (first time, or after changing package.json)
pnpm dev        # start Vite dev server on localhost:3000
pnpm build      # production build to frontend/dist/
```

### Backend -- always cd backend first:
```
./mvnw spring-boot:run    # start Spring Boot on localhost:8080
./mvnw compile            # just compile
./mvnw test               # run tests
```
You run them in two separate terminals when developing. They're independent processes.

### How the frontend talks to the backend

I configured a proxy in frontend/vite.config.ts:
```json
{
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```
During development, when the frontend makes a request to /api/anything, Vite intercepts it and forwards it to localhost:8080 (where Spring Boot is running). The browser only talks to localhost:3000 -- it never sees the backend directly. This avoids CORS issues entirely during development.
In production, you'd deploy these separately and configure your reverse proxy (nginx, etc.) to route /api to the backend and everything else to the frontend's static files.
What gets auto-generated (don't edit these)
- frontend/src/routeTree.gen.ts -- TanStack Router generates this from your files in src/routes/. It's gitignored and regenerated on every build/dev start.
- frontend/.tanstack/ -- temp files from the router plugin. Also gitignored.
  The file-based routing pattern
  TanStack Router watches frontend/src/routes/. Any .tsx file you add there automatically becomes a route:
- routes/index.tsx -> /
- routes/about.tsx -> /about
- routes/listings/$id.tsx -> /listings/:id (dynamic param)
- routes/__root.tsx -> the layout that wraps all pages. You don't need to manually register routes. The plugin generates the route tree for you.

### Tailwind CSS 4
  Tailwind v4 works as a Vite plugin (@tailwindcss/vite) -- no config file needed. It scans your source files for class names automatically. Your CSS entry point is frontend/src/index.css with just @import 'tailwindcss';. That single line gives you the full utility class library. Customization is done with CSS @theme directives directly in CSS rather than a tailwind.config.js file (which was the v3 approach).


Your confusion is completely reasonable, and it stems from the tsconfig.node.json / tsconfig.app.json split making it sound like there are two runtime environments for your frontend code. Let me clear this up.

# Your SPA: everything runs in the browser

Your intuition that "ALL code in the frontend directory will be bundled into a single JavaScript bundle and run only in the browser" is correct. That is exactly what happens. Every .tsx file in src/ gets compiled and bundled into static files (HTML, CSS, JS) that are served to and executed by the browser. There is no server-side rendering. There is no Node process running your SolidJS code.

The tsconfig.node.json confused you because of its name. Here's what it actually covers:

tsconfig.node.json does NOT mean "frontend code that runs on a server." It covers exactly one file: vite.config.ts. That file is a build tool configuration script. It runs during development (when you type pnpm dev) and during build (when you type pnpm build), on your machine, in Node. It is not part of your application. It never gets bundled. It never reaches the browser. It's the equivalent of pom.xml for the backend -- it configures the build, it doesn't ship.
So the split is:

| tsconfig           | What it covers                      | Where that code runs                                  |
| ------------------ | ----------------------------------- | ----------------------------------------------------- |
| tsconfig.app.json  | src/**/*.tsx -- your actual app     | In the browser, after Vite bundles it                 |
| tsconfig.node.json | vite.config.ts -- build tool config | On your machine in Node, during pnpm dev / pnpm build |
You can tell from the file structure: everything in src/ is browser code. Everything outside src/ (config files) is tooling that runs on your machine.
The full picture: what processes exist
Development (pnpm dev + ./mvnw spring-boot:run)

There are exactly three processes:

1. Vite dev server (Node process, port 3000) -- This is NOT your SolidJS app. This is a development tool. It watches your src/ files, transforms them on-the-fly (TypeScript -> JS, JSX -> JS, Tailwind -> CSS), and serves them to the browser. It also provides hot module replacement (HMR) so when you save a file, the browser updates without a full reload.
2. Your browser -- Loads index.html from the Vite dev server, which pulls in your JS bundle, which boots SolidJS, which renders your UI. All your application code (src/) executes here. The SolidJS app does NOT run in its own separate process -- it runs inside the browser's JavaScript engine, in the same process as the browser tab.
3. Spring Boot (JVM process, port 8080) -- Your backend. Serves API responses.

### The flow of a page load in dev:

Browser requests http://localhost:3000
-> Vite dev server responds with index.html
-> Browser parses index.html, sees `<script src="/src/index.tsx">`
-> Browser requests /src/index.tsx from Vite
-> Vite transforms it (TSX -> JS) on the fly, sends it back
-> Browser executes it -> SolidJS boots -> your app renders

### The flow of an API call in dev:

Your SolidJS code calls fetch('/api/listings')
-> Browser sends request to localhost:3000/api/listings
-> Vite dev server sees it starts with /api
-> Vite proxies (forwards) the request to localhost:8080/api/listings
-> Spring Boot handles it, returns JSON
-> Vite forwards the response back to the browser
-> Your SolidJS code receives the JSON

## What the Vite proxy actually does

The proxy exists to solve one specific problem: the browser's same-origin policy. During development, your frontend is on localhost:3000 and your backend is on localhost:8080. These are different origins. If your browser JavaScript tried to fetch('http://localhost:8080/api/listings') directly, the browser would block it as a cross-origin request (unless the backend sent CORS headers).
The proxy makes it so your frontend code just calls fetch('/api/listings') -- a relative URL. The browser sends this to localhost:3000 (where it loaded the page from). Vite intercepts it and forwards it to localhost:8080 server-side. The key insight: the same-origin policy is a browser restriction. Vite's proxy runs in Node, not in the browser, so it can talk to any server freely. From the browser's perspective, the API response came from localhost:3000 -- same origin, no CORS issue.
Production (pnpm build output)
In production, Vite is gone entirely. pnpm build produces static files in frontend/dist/:

```
dist/
  index.html
  assets/
    index-XXXX.js    (your entire SolidJS app, minified)
    index-XXXX.css   (your Tailwind styles)
```

These are just files. You serve them with any static file server (nginx, a CDN, S3, or even Spring Boot's own static resource serving). There is no Node process. No Vite. The browser downloads these files and runs the JS. The only "server" is whatever serves those static files and your Spring Boot backend.
In production, the proxy doesn't exist anymore (Vite isn't running), so you'd configure your deployment's reverse proxy (typically nginx) to route `/api/*` to Spring Boot and everything else to the static files.
Summary
- src/ = browser code. All of it. No exceptions in your setup.
- Config files outside src/ (vite.config.ts, tsconfig.*.json, package.json) = tooling that runs on your machine during development/build. Never reaches the browser.
- The SolidJS app is not a separate process. It runs inside the browser tab.
- Vite dev server is a development convenience: it transforms files, serves them, and proxies API calls. In production it doesn't exist.
- The two tsconfigs are about TypeScript checking code with the right assumptions (browser APIs vs. Node APIs), not about shipping code to two different runtimes.