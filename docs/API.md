# API REST

Referencia de la API del backend. La **fuente autoritativa** es el esquema
OpenAPI que genera la propia aplicación:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Este documento resume los recursos y las reglas de acceso. No pretende listar
cada método y cada campo —para eso usa Swagger, que siempre está sincronizado con
el código.

---

## Convenciones

- Base: `/api`.
- Formato: JSON (`application/json`).
- Fechas: ISO-8601 (`yyyy-MM-dd`, `yyyy-MM-dd'T'HH:mm:ss`).
- Paginación: parámetros `page`, `size` y `sort` (Spring Data) donde aplique.
- Errores: respuesta uniforme desde el manejador global, con código HTTP
  adecuado (`400`, `401`, `403`, `404`, `409`, `422`, `500`).

---

## Autenticación y sesión

Prefijo: `/api/auth`

| Endpoint | Descripción | Acceso |
|---|---|---|
| `POST /api/auth/login` | Inicia sesión (staff o cliente) | Público |
| `POST /api/auth/register` | Registro de cliente | Público |
| `POST /api/auth/refresh` | Renueva el access token | Público (cookie) |
| `POST /api/auth/logout` | Cierra sesión y limpia cookies | Autenticado |
| `GET /api/auth/me` | Datos del usuario actual | Autenticado |
| `POST /api/auth/google` | Inicio de sesión con Google | Público |
| `POST /api/auth/forgot-password` | Solicita código de recuperación | Público |
| `POST /api/auth/verify-reset-code` | Verifica el código | Público |
| `POST /api/auth/reset-password` | Restablece la contraseña | Público |
| `POST /api/auth/completar-perfil` | Completa el perfil | Autenticado |
| `/api/auth/vincular-cliente/**` | Vincula cuenta con ficha de cliente | Autenticado |

Tras un `login` correcto, el servidor emite **cookies `HttpOnly`** con el access
token y el refresh token. El cliente debe enviarlas en las siguientes peticiones
(`credentials: include`).

---

## Sitio público

| Prefijo | Descripción | Acceso |
|---|---|---|
| `/api/public/**` | Datos y operaciones del sitio público | Público |
| `/api/public/checkout` | Inicia el checkout de una reserva | Público |
| `/api/public/reservas` | Reserva en línea del visitante | Público |
| `/api/public/payments` | Configuración pública de pasarelas | Público |
| `GET /api/resenas/**` | Listado de reseñas | Público |
| `POST /api/pqrs` | Envío de PQRS | Público |
| `/api/webhooks` | Webhooks de pasarelas de pago | Público (validado) |

---

## Reservas y disponibilidad

| Prefijo | Descripción | Acceso |
|---|---|---|
| `/api/reservas` | CRUD de reservas, check-in/check-out | Staff |
| `/api/reservas/mis-reservas` | Reservas del cliente autenticado | Cliente |
| `/api/reservas/{reservaId}/consumos` | Consumos de la tienda por reserva | Staff |
| `/api/bloqueos` | Bloqueos de disponibilidad | Staff |
| `/api/habitaciones` | Inventario de habitaciones | Staff / lectura pública |
| `/api/tipos-habitacion` | Tipos y tarifas | Staff / lectura pública |
| `/api/precios-temporales` | Precios por temporada | Staff |

**Ciclo de vida típico de una reserva**: creación → confirmación → pago del
anticipo → check-in → consumos → check-out. Los estados se modelan como
enumeraciones en el dominio.

---

## Operación diaria

| Prefijo | Descripción | Acceso |
|---|---|---|
| `/api/clientes` | Huéspedes/clientes | Staff |
| `/api/caja` | Turnos, apertura/cierre y movimientos | Staff |
| `/api/pagos` | Registro y consulta de pagos | Staff |
| `/api/pagos/desglose` | Desglose por origen (alojamiento, tienda, …) | Staff |
| `/api/registros` | Registro de huéspedes | Staff |
| `/api/alertas` | Alertas operativas | Staff |
| `/api/pqrs` | Gestión de PQRS | Staff |
| `/api/dashboard` | Métricas y reportes | Staff |
| `/api/sse` | Notificaciones en tiempo real | Autenticado |

---

## Administración

| Prefijo | Descripción | Acceso |
|---|---|---|
| `/api/users` | Cuentas de usuario y permisos | Admin |
| `/api/empleados` | Personal del hotel | Admin |
| `/api/audit-logs` | Historial de auditoría | Admin |
| `/api/backups` | Respaldos de base de datos | Admin |
| `/api/admin/notificaciones` | Notificaciones administrativas | Admin |
| `/api/catalogos` | Catálogos de referencia | Staff |

---

## Roles

| Rol | Alcance |
|---|---|
| `CLIENTE` | Sitio público, sus propias reservas y su perfil. |
| `RECEPCIONISTA` | Operación diaria: reservas, caja, clientes, pagos, PQRS. |
| `ADMIN` | Todo lo anterior + usuarios, empleados, auditoría y backups. |

---

## Ejemplo de uso

```bash
# 1) Iniciar sesión (guarda las cookies en cookies.txt)
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"cambia-esta-clave"}'

# 2) Consumir un recurso protegido reutilizando la cookie
curl -b cookies.txt http://localhost:8080/api/auth/me

# 3) Renovar el token cuando el access caduque
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/refresh
```

---

## Regenerar el cliente del frontend

Con el backend en marcha:

```bash
cd frontend
pnpm generate:api
```

Esto lee `http://127.0.0.1:8080/v3/api-docs` y actualiza
`src/api/generated/`.
