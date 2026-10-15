# Hotel Cacique Bolívar — Frontend 🏨✨

Aplicación web moderna para la gestión del **Hotel Cacique Bolívar**. Construida con React 19 y TypeScript 5,
ofrece una experiencia fluida tanto para huéspedes (reservas públicas) como para el staff administrativo
(dashboard completo con roles).

> Módulo del monorepo de demostración. Consulta el [README raíz](../README.md),
> la [arquitectura](../docs/ARCHITECTURE.md) y la [guía de desarrollo](../docs/DEVELOPMENT.md).
> Los datos de contacto y el NIT son **ficticios**; no se versionan secretos.

---

## Stack Tecnológico 🛠️

| Capa              | Tecnología                                    |
| ----------------- | --------------------------------------------- |
| Lenguaje          | TypeScript 5.9                                |
| Framework         | React 19                                      |
| Build             | Vite 7                                        |
| Package Manager   | pnpm                                          |
| Routing           | React Router DOM v7                           |
| Estado servidor   | TanStack Query v5                             |
| Formularios       | react-hook-form v7 + Zod v4                   |
| Estilos           | Tailwind CSS 4 + shadcn/ui (radix-ui)         |
| Íconos            | lucide-react + react-icons                    |
| Gráficas          | Recharts 3.9                                  |
| Autenticación     | Google OAuth + JWT en cookies                 |
| API Client        | Axios vía Orval (generado desde OpenAPI)      |
| Toasts            | sonner                                        |
| Animaciones       | motion (framer-motion v12)                    |
| Tipografía        | Geist (variable), Inter, Playfair Display     |

---

## 🧭 Rutas

### Públicas

| Ruta              | Página                   |
| ----------------- | ------------------------ |
| `/`               | Landing Page             |
| `/reservar`       | Reservas públicas        |
| `/privacidad`     | Política de privacidad   |
| `/terminos`       | Términos y condiciones   |
| `/ayuda`          | Centro de ayuda          |
| `/mi-perfil`      | Perfil del cliente (modal) |
| `/mis-reservas`   | Reservas del cliente (modal) |
| `/hotel/login`    | Login del staff          |

> El inicio de sesión de clientes se abre en un **modal** desde la barra de
> navegación pública (no tiene ruta propia).

### Dashboard (roles: `ADMIN` · `RECEPCIONISTA`)

| Ruta                                    | Página                    |
| --------------------------------------- | ------------------------- |
| `/dashboard/home`                       | Panel principal           |
| `/dashboard/clientes`                   | Gestión de clientes       |
| `/dashboard/clientes/nuevo`             | Nuevo cliente             |
| `/dashboard/clientes/:id`               | Detalle del cliente       |
| `/dashboard/clientes/:id/editar`        | Editar cliente            |
| `/dashboard/reservas`                   | Listado de reservas       |
| `/dashboard/reservas/nueva`             | Nueva reserva             |
| `/dashboard/reservas/:id`               | Detalle de reserva        |
| `/dashboard/reservas/:id/editar`        | Editar reserva            |
| `/dashboard/habitaciones`               | Habitaciones              |
| `/dashboard/habitaciones/nuevo`         | Nueva habitación          |
| `/dashboard/habitaciones/:id`           | Detalle de habitación     |
| `/dashboard/habitaciones/:id/editar`    | Editar habitación         |
| `/dashboard/habitaciones/tipos/nuevo`   | Nuevo tipo de habitación  |
| `/dashboard/habitaciones/tipos/:id`     | Detalle de tipo           |
| `/dashboard/habitaciones/tipos/:id/editar` | Editar tipo           |
| `/dashboard/ocupacion`                  | Disponibilidad / ocupación|
| `/dashboard/empleados`                  | Gestión de empleados      |
| `/dashboard/caja`                       | Módulo de caja            |
| `/dashboard/usuarios`                   | Usuarios del sistema      |
| `/dashboard/usuarios/nuevo`             | Nuevo usuario             |
| `/dashboard/usuarios/:id`               | Detalle de usuario        |
| `/dashboard/usuarios/:id/editar`        | Editar usuario            |
| `/dashboard/historial-acciones`         | Auditoría / log           |
| `/dashboard/ajustes`                    | Configuración             |

---

## 📁 Estructura del Proyecto

```
src/
├── api/               # Capa de API (generada por Orval + instancia Axios)
├── app/               # Router, layouts, providers
├── components/        # Componentes compartidos (shadcn/ui + DataTable)
├── features/          # 26 módulos funcionales
│   ├── home/          # Landing page
│   ├── booking/       # Reservas públicas
│   ├── auth-staff/    # Login del staff
│   ├── auth-client/   # Autenticación de clientes (Google)
│   ├── dashboard/     # Panel de control con widgets analíticos
│   ├── clientes/      # CRUD de clientes
│   ├── reservas/      # CRUD de reservas con operaciones
│   ├── habitaciones/  # CRUD de habitaciones y tipos
│   ├── disponibilidad/# Vista de ocupación en tiempo real
│   ├── empleados/     # Gestión de empleados
│   ├── caja/          # Caja registradora y turnos
│   ├── pagos/         # Gestión de pagos
│   ├── turnos/        # Gestión de turnos
│   ├── alertas/       # Alertas de clientes
│   ├── users/         # Usuarios del sistema
│   ├── historial-acciones/  # Log de auditoría
│   ├── settings/      # Configuración
│   └── legal/         # Páginas legales
├── hooks/             # Hooks compartidos
├── layouts/           # Layouts (dashboard, público)
└── lib/               # Utilidades, tipografías
```

---

## 🚀 Comandos

```bash
pnpm dev              # Servidor de desarrollo en :5173
pnpm build            # Typecheck + build de producción
pnpm lint             # ESLint
pnpm preview          # Preview del build de producción
pnpm generate:api     # Regenerar API client desde OpenAPI (requiere backend en :8080)
```

---

## 📐 Convenciones del Proyecto

| Convención                  | Detalle                                                      |
| --------------------------- | ------------------------------------------------------------ |
| **Import alias**            | `@/*` → `src/*` (configurado en tsconfig + vite-tsconfig-paths) |
| **Tipos**                   | `verbatimModuleSyntax` activo → usar `import type` para imports de solo tipos |
| **Estilos**                 | Tailwind CSS 4 via `@tailwindcss/vite` (sin PostCSS). Variables CSS en OKLCH |
| **Componentes UI**          | shadcn/ui en `src/components/ui/`. `DataTable<T>` genérica para listados |
| **API Client**              | Generado por Orval en `src/api/generated/`. Axios con interceptor que maneja refresh automático de JWT y logout si falla |
| **Formularios**             | react-hook-form + Zod. Esquemas en `schemas/` dentro de cada feature |
| **Toasts**                  | sonner (`<Toaster />` en `App.tsx`)                          |
| **Páginas**                 | Default export. Utilidades: named exports                    |
| **Manejo de errores**       | Los errores del backend se normalizan a `ApiError { message, fieldErrors?, status }` |

---

## 🔐 Flujo de Autenticación

1. El usuario inicia sesión vía formulario (staff) o Google OAuth (clientes)
2. El backend responde con **access_token** y **refresh_token** en cookies httpOnly
3. El interceptor de Axios renueva automáticamente el token al recibir un 401
4. Si el refresh falla, se llama a `authContext.logout()` y se redirige al login
