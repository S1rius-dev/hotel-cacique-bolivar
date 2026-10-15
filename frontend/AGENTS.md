# AGENTS.md — Frontend

Guía breve para trabajar en el módulo `frontend/` del monorepo de demostración
**Hotel Cacique Bolívar**. Para el contexto general, consulta el
[README raíz](../README.md) y [`docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md).

## Comandos (desde `frontend/`)

| Comando | Qué hace |
|---|---|
| `pnpm dev` | Servidor Vite |
| `pnpm build` | `tsc -b` (referencias de proyecto) → `vite build` + prerender |
| `pnpm lint` | ESLint (flat config) sobre `*.{ts,tsx}` |
| `pnpm preview` | Sirve el build de producción |
| `pnpm generate:api` | Orval: lee `localhost:8080/v3/api-docs` y escribe en `src/api/generated/` |

No hay infraestructura de tests ni de formateo.

## Arquitectura

- Entrada: `index.html` → `src/main.tsx` (React 19 en `#root`). El orden de los
  providers importa: `GoogleOAuthProvider` > `BrowserRouter` >
  `QueryClientProvider` > `AuthProvider`.
- Rutas (`react-router-dom` v7) en `src/app/routes/`: públicas (`/`, `/reservar`,
  `/privacidad`, `/terminos`, `/ayuda`, `/mi-perfil`, `/mis-reservas`, `/pago`),
  login de staff (`/hotel/login`) y dashboard protegido (`/dashboard/*`,
  `ADMIN` + `RECEPCIONISTA`). El login de clientes es un **modal** del sitio público.
- Módulos en `src/features/` (26) con subcarpetas `pages/`, `components/`,
  `hooks/`, `schemas/` (zod) y, a veces, `fields/`, `constants/`, `types/`, `utils/`.
- Capa API: código generado por Orval en `src/api/generated/` (uno por
  controlador), usando el mutador `customInstance` (Axios).
- Auth: cookies con rotación de refresh token. `AuthContext` expone
  `{ user, loading, login, logout, refreshUser }`. El interceptor de Axios
  refresca ante un `401` y hace logout si el refresh falla.

## Convenciones

- Alias `@/` → `src/`.
- `verbatimModuleSyntax` activo: usa `import type` para imports de solo tipos.
- `noUnusedLocals` y `noUnusedParameters` activos.
- Tailwind v4 vía `@tailwindcss/vite`; tokens de color en OKLCH.
- shadcn/ui en `src/components/ui/`; `DataTable<T>` genérica para listados.
- TanStack Query v5: un hook tipado por método de controlador.
- Formularios: react-hook-form + Zod en `schemas/` de cada feature.
- Páginas: export default. Utilidades: named exports.
- Errores normalizados a `ApiError { message, fieldErrors?, status? }`.

## Nota sobre el cliente API

`pnpm generate:api` requiere el backend en `localhost:8080`. Si no está
disponible, el build usa la última versión generada y **no** regenera el cliente.

## Integración continua

El CI del monorepo (`.github/workflows/ci.yml`) solo compila y prueba
(`pnpm install --frozen-lockfile`, `pnpm lint` con `continue-on-error`, y
`pnpm build` con `PRERENDER_NO_BROWSER=1`). No hay jobs de despliegue.
