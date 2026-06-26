#!/bin/bash
set -euo pipefail

# build-css.sh
# Compiles the Tailwind CSS source into the static output directory.
# Used by Maven during builds and can be run manually.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

echo "Building CSS..."
npm run build-prod

echo "CSS build complete."
