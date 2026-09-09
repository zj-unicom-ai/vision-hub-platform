# Vision Hub Frontend

This directory contains the Vue 3 management console for the open-source Vision Hub Platform.

## Requirements

- Node.js 18+
- npm 9+

## Configuration

The development proxy forwards `/api` requests to the backend service. Configure the backend address with:

```bash
export VITE_API_BASE_URL="http://localhost:18080"
```

If the variable is not set, the proxy uses `http://localhost:18080`.

## Install

```bash
npm install
```

## Develop

```bash
npm run dev
```

The local frontend server starts on port `3000` by default.

## Build

```bash
npm run build
```

Serve the generated `dist/` with any static server that forwards `/api` to the web service and falls back to `index.html` for history-mode routing. A ready-to-use nginx configuration is provided in [`docker/nginx.conf`](../docker/nginx.conf).

## Docker Deployment

The console is included in the all-in-one Docker Compose stack in the repository-level [`docker/`](../docker) directory (built by `docker/frontend.Dockerfile` and served by nginx):

```bash
cd ../docker
docker compose up -d --build
```

Open <http://localhost> and log in with `unicom` / `ZJ_Unicom`.

## Open-Source Scope

The console includes the core visual algorithm workflow: algorithm management, model management, device access, task orchestration, event records, and execution logs. Scene governance and device organization views are not included in this edition.
