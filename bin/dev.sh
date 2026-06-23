#!/bin/bash
set -euo pipefail

# dev.sh
# Runs the Spring Boot application alongside the Tailwind CSS watcher.
# This gives you hot reload for both server-side code and styles.
#
# Usage:
#   ./bin/dev.sh
#
# What happens:
#   1. Tailwind CSS starts in --watch mode via npm, rebuilding app.css when templates or CSS change.
#   2. Spring Boot starts with DevTools enabled, auto-reloading when static resources change.
#   3. Press Ctrl+C to stop both processes.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

# Start Tailwind in watch mode in the background
echo "Starting Tailwind CSS watcher..."
npm run watch &

TAILWIND_PID=$!

# Ensure Tailwind is killed when this script exits
cleanup() {
    echo ""
    echo "Shutting down Tailwind watcher..."
    kill "${TAILWIND_PID}" 2>/dev/null || true
    wait "${TAILWIND_PID}" 2>/dev/null || true
    echo "Done."
}
trap cleanup EXIT INT TERM

echo "Tailwind watcher started (PID: ${TAILWIND_PID})."
echo ""
echo "Starting Spring Boot application..."
echo ""

# Run Spring Boot in the foreground so Ctrl+C propagates correctly
./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    "$@"
