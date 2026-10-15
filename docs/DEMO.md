# Guía de demostración

Este documento muestra cómo ejecutar la **demo pública** de Hotel Cacique Bolívar
de principio a fin, qué datos de ejemplo existen, cómo probar los flujos más
importantes y qué **no** incluye este repositorio (por seguridad).

---

## 1. Objetivo

La demo sirve para mostrar las capacidades del sistema (reserva online, panel
operativo, caja, pagos, etc.) sin exponer datos reales. Todos los contactos y
identificadores son ficticios. Los únicos datos poblados automáticamente son:

- **Tipos de habitación** y **habitaciones** de ejemplo.
- **Catálogo de municipios** (DANE, dato público).
- **Usuario administrador** (según tu `.env`).

No hay clientes, reservas ni pagos precargados.

---

## 2. Requisitos para la demo

- Backend y frontend en ejecución (ver [README.md](../README.md#inicio-rápido)).
- Navegador moderno (Chrome/Edge/Firefox).
- MySQL 8 (o Docker). No requiere servicios externos para probar el flujo base.

---

## 3. Datos de inicio de sesión

Tras copiar `backend/.env.example` a `backend/.env`:

```env
ADMIN_EMAIL=admin@example.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=HotelAdmin2025!
```

Puedes cambiar estos valores a tu gusto. El usuario se crea automáticamente
(idempotente) al arrancar el backend, con rol `ADMIN`.

**Login de staff:** `http://localhost:5173/hotel/login`

**Login de cliente:** se abre en un **modal** desde la barra de navegación del
sitio público (botón de iniciar sesión), no tiene ruta propia. El cliente se crea
al registrarse o al completar una reserva en línea.

---

## 4. Flujos para probar

### A. Reserva pública (cliente anónimo)

1. Ir a `http://localhost:5173/`
2. Hacer clic en "Reservar" o ir a `/reservar`
3. Seleccionar fechas, número de huéspedes y tipo/habitación
4. Completar los datos del huésped y crear la reserva
5. Seguir el flujo de pago (anticipo). En el entorno de demo las pasarelas no
   procesan dinero, pero el flujo de checkout completo funciona.

### B. Panel operativo (recepcionista/admin)

1. Iniciar sesión en `/hotel/login` con las credenciales de admin
2. Ir a `http://localhost:5173/dashboard`
3. Probar: Disponibilidad, Reservas, Caja (apertura/cierre), Tienda (consumos),
   Clientes, Pagos, PQRS

### C. Gestión de habitaciones y tipos

1. En Dashboard → Habitaciones: listar/crear/editar
2. En Dashboard → Tipos de habitación: gestionar tarifas y descripciones

### D. Auditoría y respaldos (solo ADMIN)

1. Dashboard → Historial de acciones (`/dashboard/historial-acciones`)
2. Dashboard → Respaldos (`/dashboard/backups`)
   - El módulo de backups funciona con la configuración local. Requiere `mysqldump`
     y una ruta de almacenamiento válida.

---

## 5. ¿Qué no incluye esta demo?

| Elemento | Estado | Explicación |
|---|---|---|
| Datos de clientes reales | **No incluido** | Se eliminó `migration-client.csv`. No hay huéspedes, reservas ni pagos reales. |
| Datos de contacto reales | **Sustituidos** | Teléfono, email, NIT y otros identificadores son **ficticios**. |
| Credenciales de producción | **No incluidas** | SMTP, Wompi/Bold, Google OAuth usan valores de ejemplo o están vacíos. |
| Volcados de BD (`.sql.gz`) | **Excluidos** | Ignorados por `.gitignore`. |
| Backups de producción | **Excluidos** | Carpeta `backend/backups/` ignorada. |

---

## 6. Recomendaciones para la demo

- Muestra primero el **sitio público** → reserva → checkout, para resaltar la
  experiencia del huésped.
- Luego muestra el **panel de staff** → mapa de disponibilidad, caja, pagos.
- Enfatiza: contrato API único (Orval), seguridad (cookies HttpOnly), pruebas
  automáticas (165 tests) y esquema versionado (Flyway).
- **Sé honesto**: menciona que es una demo, los datos son ficticios y las
  pasarelas están en modo demostrativo.
- No publiques capturas que contengan información que consideres sensible, aunque
  este repositorio ya la haya depurado.

---

## 7. Problemas comunes durante la demo

| Problema | Solución |
|---|---|
| No aparecen habitaciones en `/reservar` | Espera a que el backend termine de arrancar (siembra al inicio). |
| Login de staff falla | Revisa `ADMIN_EMAIL` / `ADMIN_PASSWORD` en `backend/.env`. |
| CORS en localhost | Verifica `CORS_ALLOWED_ORIGINS=http://localhost:5173` (default OK). |
| Frontend no conecta con API | Asegúrate de que `VITE_API_URL` no esté forzado o que backend esté en 8080. |

---

## 8. Próximos pasos para una demo "en vivo"

1. Configura SMTP real (opcional) para recuperación de contraseña.
2. Usa credenciales de sandbox de tu pasarela (Wompi/Bold) si quieres probar el
   webhook completo.
3. Configura Google OAuth si quieres habilitar login con Google.
4. Desactiva Swagger en producción (ya está deshabilitado con perfil `prod`).

**Nota:** Este repositorio **no está pensado para desplegarse tal cual en
producción** sin una revisión de seguridad. Es una demostración pública para
evaluación técnica y portafolio.
