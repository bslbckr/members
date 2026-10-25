# GucMembers - Monorepo Agent Context

## Overview

This repository contains a complete member management system composed of two sub-projects:

- **`web-ui/`** - Angular v22 web-client
- **`members-backend/`** - Quarkus Java backend

## Projects

### Web UI (`web-ui/`)

Angular v22 web-client for the member management system.

See [`web-ui/AGENTS.md`](./web-ui/AGENTS.md) for details.

### Backend (`members-backend/`)

Java backend service for managing member operations.

See [`members-backend/AGENTS.md`](./members-backend/AGENTS.md) for details.

## Architecture

The system follows a frontend-backend separation:

```
├── web-ui/          # Angular 22 frontend client
│   ├── src/
│   │   ├── app/
│   │   └── assets/
│   ├── index.html
│   └── package.json
│
└── members-backend/  # Quarkus 3.40.1.1 backend
    ├── src/
    │   ├── main/
    │   │   ├── java/de/guc/
    │   │   └── resources/
    │   └── test/
    │
    └── pom.xml
```

## Communication

The frontend communicates with the backend service. The proxy configuration at `web-ui/src/proxy.conf.json` routes requests during development.

## Building

- **Frontend**: `cd web-ui && npm run build`
- **Backend**: `cd members-backend && mvn package` or `mvn compile quarkus:dev` (dev mode)
