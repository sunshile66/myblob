# Frontend Architecture

## Package Manager

- Preferred package manager: `pnpm`
- Workspace root: `D:\code\Myblob`
- Frontend package: `blog-frontend`

## Recommended Commands

From workspace root:

```powershell
pnpm install
pnpm dev:frontend
pnpm build:frontend
pnpm typecheck:frontend
```

From `blog-frontend`:

```powershell
pnpm install
pnpm dev
pnpm build
pnpm typecheck
```

## Directory Strategy

- `src/app`: app bootstrap, root app component, router composition
- `src/features`: feature-first modules such as `home`, `tools`, `search`
- `src/shared`: shared layouts, navigation, reusable UI wrappers
- `src/pages`, `src/router`, `src/layout`, `src/components`: legacy compatibility layer during migration

## Migration Notes

- Old paths are temporarily kept as wrappers so existing imports and routes do not all need to change at once.
- New code should prefer `@app`, `@features`, and `@shared` aliases.
- `pnpm-lock.yaml` is not committed yet because the current environment does not have a usable `pnpm` binary installed.
