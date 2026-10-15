# Backend — API REST (Spring Boot)

Módulo backend del sistema **Hotel Cacique Bolívar**. Expone la API REST que
concentra la lógica de negocio: reservas, habitaciones, clientes, pagos, caja,
usuarios, auditoría, PQRS y tableros.

> Forma parte del monorepo de demostración. Consulta el
> [README raíz](../README.md), la [arquitectura](../docs/ARCHITECTURE.md) y la
> [referencia de la API](../docs/API.md). Los datos personales y de contacto han
> sido sustituidos por valores ficticios; no se versionan credenciales.

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 (`release 21`) |
| Framework | Spring Boot 4.0.6 |
| Seguridad | Spring Security + JWT (jjwt) + BCrypt |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8 (H2 en pruebas) |
| Migraciones | Flyway (`db/migration`) |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Mapeo | MapStruct + Lombok |
| Validación | Jakarta Bean Validation |
| Build | Maven Wrapper (`mvnw`) |
| Auth social | Google OAuth (opcional) |
| Tareas | `@EnableScheduling` activo |

---

## Dominio

### Reservas

Ciclo de vida: `PENDIENTE → CONFIRMADA → EN_CURSO → FINALIZADA / CANCELADA / NO_SHOW`.
Cada habitación de la reserva rastrea su estado (`RESERVADA → OCUPADA → LIBERADA → CANCELADA`),
con check-in/check-out parcial, huéspedes por habitación, cargos/descuentos/
cortesías y código generado automáticamente.

### Habitaciones

- Tipos con capacidad base/máxima, precio base y precio por persona extra.
- Estados: `DISPONIBLE`, `RESERVADA`, `OCUPADA`, `EN_LIMPIEZA`, `MANTENIMIENTO`, `INHABILITADA`.

### Clientes

- Identificación con tipos colombianos y municipio de expedición.
- Alertas de comportamiento (económico, hurto, conflictivo, fumador, daños).
- La importación masiva desde CSV existe en el código pero está **desactivada**
  en la demo (`MIGRACION_CLIENTES_AUTO=false`); no se distribuye ningún archivo
  de datos de clientes.

### Pagos y caja

- Tipos de pago: `TOTAL`, `PARCIAL`, `ANTICIPO`, `DEPÓSITO_GARANTÍA`.
- Métodos: efectivo, tarjetas, transferencia, cheque y otros.
- Cajas nominales y turnos (mañana/tarde/noche) con apertura, cierre y arqueo.

### Auditoría

- `audit_logs` registra usuario, entidad, acción y timestamp de las operaciones críticas.

---

## Estructura de paquetes

```
com.hotelcaciquebolivar.reservas/
├── config/        # Seguridad, CORS, OpenAPI, inicializadores, perfiles
├── controller/    # 29 controladores REST (por dominio y subpaquetes)
├── dto/           # Objetos de transferencia
├── entity/        # Entidades JPA con lógica de dominio
├── exception/     # Excepciones y manejador global
├── mapper/        # Mappers MapStruct
├── repository/    # Repositorios Spring Data JPA
├── security/      # JWT, filtros, entry point
├── service/       # Lógica de negocio
└── …
```

---

## Endpoints principales

| Prefijo | Descripción |
|---|---|
| `/api/auth` | Login, registro, refresh, logout, Google, recuperación |
| `/api/public/**` | Disponibilidad, checkout y pagos del sitio público |
| `/api/reservas` | CRUD y operaciones de reservas |
| `/api/habitaciones`, `/api/tipos-habitacion` | Inventario y tarifas |
| `/api/clientes` | Huéspedes |
| `/api/pagos`, `/api/caja` | Pagos, caja y turnos |
| `/api/empleados`, `/api/users` | Personal y cuentas |
| `/api/pqrs`, `/api/resenas`, `/api/alertas` | PQRS, reseñas y alertas |
| `/api/dashboard`, `/api/sse` | Métricas y eventos en tiempo real |
| `/api/audit-logs`, `/api/backups` | Auditoría y respaldos |

Referencia completa y siempre actualizada en Swagger UI (`/swagger-ui/index.html`).

---

## Comandos

```bash
./mvnw spring-boot:run   # arranca en :8080 (usa el perfil activo)
./mvnw test              # pruebas (perfil test + H2)
./mvnw clean package     # genera el JAR
./mvnw clean verify      # compila + pruebas (lo que corre el CI)
```

En Windows usa `.\mvnw.cmd`. También existe `run-dev.ps1`, que copia
`.env.example` a `.env` si falta y arranca con el perfil `dev`.

---

## Perfiles

| Perfil | Uso | Base de datos | Configuración |
|---|---|---|---|
| `dev` | Desarrollo local | MySQL (o H2) | `.env` + `application-dev.yml` |
| `test` | Pruebas automatizadas | H2 en memoria | `src/test/resources/application-test.yml` |
| `prod` | Producción | MySQL | Variables de entorno del sistema |

`application.yml` es común e importa `.env` con
`spring.config.import=optional:file:.env[.properties]`. Los secretos **nunca** se
versionan: en `prod` se inyectan por variables de entorno y el arranque falla si
falta alguno (`ProdEnvironmentValidator`).

### Desarrollo

```powershell
Copy-Item .env.example .env   # edita .env con tus valores (no se versiona)
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run    # o: .\run-dev.ps1
```

### Pruebas

```powershell
$env:SPRING_PROFILES_ACTIVE="test"; .\mvnw.cmd test
```

No requiere MySQL ni `.env`: usa H2 en memoria.

---

## Seguridad

- Autenticación **stateless** con JWT en cookies **HttpOnly** (access + refresh).
- CSRF deshabilitado (arquitectura stateless).
- Roles: `ADMIN`, `RECEPCIONISTA`, `CLIENTE`.
- Endpoints públicos: `/api/auth/**`, `/api/public/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- CORS restringido por la propiedad `app.cors.allowed-origins` (por defecto, solo
  el frontend local). No hay direcciones privadas ni dominios de producción en el
  código.

---

## Pruebas

JUnit 5 + Mockito sobre H2. Cubren entidades de dominio, cálculo de precios y
anticipos, desglose de pagos, importación/backups y el contexto Spring completo
(165 pruebas, 0 fallos en la verificación de este árbol).
