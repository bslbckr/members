# GucMembers Web UI - Agent Context

## Overview

Angular v22 web-client for the GUC member management system.

## Tech Stack

- **Framework**: Angular v22 with @Angular/cli v22.2.4
- **State Management**: NgRx (effects, store, operators, store-devtools)
- **UI**: Angular Material v22.2.4
- **Build**: Bundle-aware (esbuild)
- **Tests**: Vitest

## Architecture

### Routes

- **/start**
  - `start.component.ts`
  - `start.component.html`
  - `start.service.ts`
  - `start.effects.ts`

- **/home**
  - `home.component.ts`

- **/on.boarding**
  - `on.boarding.component.ts`
  - `on-boarding.service.ts`
  - `onboarding.effects.ts`

- **/member/{id}**
  - `member.component.ts`
  - `member.component.html`
  - `member.component.spec.ts`
  - `member.service.ts`
  - `member.effects.ts`
  - `cancellation-dialog` subcomponent

- **/member-overview**
  - `member-overview.component.ts`
  - `member-overview.service.ts`
  - `send-mail` subcomponent
  - `send-mail.service.ts`

- **/changes**
  - `changes.component.ts`
  - `changes.component.spec.ts`
  - `change.service.ts`

### State Management Pattern

Redux-like store pattern using NgRx:

```typescript
/src/app/reducers/
  - reducers.ts
  - user.reducer.ts
  - member.reducer.ts
  - resource.reducer.ts
```

```typescript
/src/app/actions/
  - actions.ts
  - user.actions.ts
  - member.actions.ts
  - modify-member.actions.ts
  - load-member.actions.ts
  - resource.actions.ts
```

```typescript
/src/app/effects/
  (see reducer-specific effects in each reducer file)
```

### Store Structure

- `reducers/*` - state slices
- `reducers/*.ts` - state slices with tests
- `actions/*` - action creators
- `effects/*` / `start/effects/*` - side effects
- `selectors/*` - state queries

### Routing / Guards

- `/home`, `/changes` → no auth required
- `/start` → auth required
- `/member/*` → `authenticated` guard
- `/member-overview` → `has-role` guard

## Project Layout

```
src/app/
  app.component.html
  app.component.ts
  app.config.ts
  app.config.spec.ts

  routes/
    routes.config.ts

  guards/
    authenticated.guard.ts
    authenticated.guard.spec.ts
    has-role.guard.ts

  models/
    UserInfo.ts
    Member.ts
    ResourceInfo.ts

  reducers/
  effects/
    start/effects/
      start.effects.ts

  selectors/
  actions/
    actions/

  components/
    application/
      application.component.html
      application.component.ts

    start/
      start.component.*

    home/
      home.component.*

    changes/
      changes.component.*

    member/
      member.component.*
      member.service.*
      member.effects.*
      member/overviews/
      member/cancellation-dialog/

    member-overview/
      member-overview.component.*
      member-overview.service.*
      send-mail/

    on.boarding/
      on.boarding.component.*
      on-boarding.service.*
      +state/

    menu/
      ...

  services/
    start/
      resource.service.ts
    on-boarding/
      on-boarding.service.ts
    changes/
      change.service.ts
    send-mail/
      send-mail.service.ts
```

## Environment

- `src/environments/environment.ts`
- `src/environments/environment.development.ts`

## Build Outputs

`dist/guc-members/browser/` (compiled JS/CSS)

`dist/guc-members/3rdpartylicenses.txt` (licenses)

`html/` (static assets)

## Dev Server

`ng serve` → http://localhost:4200/

Proxy to backend via `src/proxy.conf.json`
