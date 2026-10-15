# AGENTS.md — Backend

Guía breve para trabajar en el módulo `backend/` del monorepo de demostración
**Hotel Cacique Bolívar**. Para el contexto general, consulta el
[README raíz](../README.md) y [`docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md).

## Arranque rápido

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run   # dev (MySQL + .env)
$env:SPRING_PROFILES_ACTIVE="test"; .\mvnw.cmd test             # test (H2)
.\mvnw.cmd clean verify                                          # compila + pruebas (CI)
```

Una sola clase de prueba:
`.\mvnw.cmd test -Dtest=PagoServiceImplTest -Dspring.profiles.active=test`

## Arquitectura

- En capas: `controller/` → `service/` → `repository/`.
- Entrada: `HotelCaciqueBolivarApplication` (`@EnableScheduling`).
- Inicializadores en `config/initializer/` (admin, habitaciones, municipios;
  la importación de clientes por CSV está **desactivada** en la demo).
- Seguridad: JWT vía `security/jwt/` (jjwt) + cookies HttpOnly.
- Lombok + MapStruct: el orden de los procesadores en `pom.xml` importa
  (`lombok-mapstruct-binding` → `lombok` → `mapstruct-processor`).

## Base de datos

- `dev`: MySQL local, `ddl-auto=none`, Flyway gestiona el esquema.
- `test`: H2 en memoria (`src/test/resources/application-test.yml`), Flyway desactivado.
- `prod`: MySQL, `ddl-auto=validate`, secretos por variables de entorno.

### Migraciones (Flyway)

- Ubicación: `src/main/resources/db/migration/`, naming `V{N}__descripcion.sql`.
- Toda entidad o cambio de esquema debe traer su migración.
- Las migraciones deben ser idempotentes (comprobar `information_schema` antes de
  ALTER/CREATE/DROP).
- **Nunca** editar una migración ya aplicada; crea una nueva que corrija.
- MySQL 8 no soporta índices parciales (`CREATE INDEX ... WHERE`).

## Documentación de código

- Javadoc breve en clases públicas y métodos de servicio (parámetros, retorno,
  efectos).
- Endpoints con `@Operation` de SpringDoc.
- Los comentarios explican el **por qué**, no el **qué**.
- Mensajes de error en español.

## Configuración

- `application.yml` común + `spring.config.import=optional:file:.env[.properties]`.
- `.env` solo en desarrollo (ignorado por Git); `.env.example` versionado con
  marcadores `CHANGEME`.
- Producción usa variables de entorno del sistema, sin `.env` (fail-fast vía
  `ProdEnvironmentValidator`).
- Mensajes de validación en `messages.properties`.

## Pruebas

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`).
- Ninguna prueba requiere una base externa: se usa H2.
- Verificación real de este árbol: 165 pruebas, 0 fallos.

## Convenciones

- API client del frontend: Orval genera desde el OpenAPI del backend.
- DTOs como records; nunca exponer entidades JPA en los controladores.
- Mappers MapStruct con `@Mapper(componentModel = "spring")`.
- Excepciones propias + `GlobalExceptionHandler`.
- Auditoría en `audit_logs`; SSE vía `SseEmitterService`.

## Integración continua

El CI del monorepo (`.github/workflows/ci.yml`) solo **compila y prueba**
(`./mvnw -B -ntp clean verify`). No hay jobs de despliegue.
