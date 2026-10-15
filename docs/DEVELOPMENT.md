# Guía de desarrollo

Cómo configurar el entorno de desarrollo, trabajar con el código y contribuir a la
demo. Todo lo indicado aquí asume que el repositorio ya existe en
`C:\Users\PC\Dev\hotel-cacique-bolivar` o en el path equivalente de tu máquina.

---

## 1. Prerequisitos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 21+ | `release 21` en `pom.xml`. Probado en JDK 25. |
| Git | 2.55+ | Gestor de versiones. |
| Node.js | 22+ | Probado en 24. |
| pnpm | 9+ | Probado en 11. |
| Docker | opcional | Para levantar MySQL local (`docker-compose.yml`). |
| MySQL | 8 | Alternativa a Docker. |

Verifica: `java -version`, `mvn -v` (o usa `mvnw`), `node -v`, `pnpm -v`.

---

## 2. Preparación del repositorio

```bash
git clone <TU_URL_PUBLICA> hotel-cacique-bolivar  # cuando se publique
cd hotel-cacique-bolivar
```

Copia las plantillas de entorno:

```bash
# Backend
cd backend
cp .env.example .env  # Windows: Copy-Item .env.example .env

# Frontend
cd ../frontend
cp .env.example .env  # Windows: Copy-Item .env.example .env
```

Ajusta `backend/.env` con tus credenciales (al menos `ADMIN_EMAIL`, `ADMIN_PASSWORD`
y `JWT_SECRET` si quieres usar un valor distinto al de demostración).

---

## 3. Base de datos

### Opción A — Docker (recomendada para desarrollo)

```bash
# Desde la raíz del monorepo
docker compose up -d
```

Datos de ejemplo del `docker-compose.yml`:
- DB: `hotelcaciquebolivar`
- Usuario: `hoteldemo`
- Contraseña: `demo_password`
- Root: `demo_root_password`

### Opción B — MySQL local

Crea la base y el usuario con los valores de `backend/.env`. Flyway aplicará
las migraciones al arrancar.

---

## 4. Ejecutar en modo desarrollo

### Backend

```bash
cd backend
.\mvnw.cmd spring-boot:run  # Windows
# ./mvnw spring-boot:run      # Linux/macOS
```

Disponible en: <http://localhost:8080>
Swagger: <http://localhost:8080/swagger-ui/index.html>

El backend arranca con el perfil `dev` por defecto. Usa H2 o MySQL según
`spring.datasource.*`.

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Disponible en: <http://localhost:5173>. El proxy/VITE_API_URL apunta a
`http://localhost:8080`.

Arranca el frontend tras el backend para que Orval y la API estén disponibles.

---

## 5. Regenerar cliente API

Cuando cambies los controladores/DTOs del backend y quieras que el frontend
reciba los nuevos tipos:

1. Arranca el backend: `.\mvnw.cmd spring-boot:run`
2. Desde `frontend/`:

```bash
pnpm generate:api
```

Orval leerá `http://127.0.0.1:8080/v3/api-docs` y regenerará
`src/api/generated/`.

No edites ese directorio manualmente.

---

## 6. Calidad: lint, tipo-check y build

### Frontend

```bash
cd frontend
pnpm lint        # ESLint
pnpm build       # tsc -b + vite build + prerender (rutas públicas)
```

`pnpm build` genera `dist/` listos para servir.

### Backend

```bash
cd backend
.\mvnw.cmd clean test    # compila y ejecuta todas las pruebas
.\mvnw.cmd test          # solo pruebas
.\mvnw.cmd package       # genera JAR en target/
```

---

## 7. Pruebas

**Backend:** 165 pruebas unitarias y de contexto, ejecutadas con JUnit 5 contra
H2 (`application-test.yml`). Causan efecto mínimo (uso de `@DataJpaTest` o
mocks) y no requieren MySQL.

**Frontend:** el chequeo es por tipos + compilación + lint. Puedes añadir
pruebas más adelante si lo deseas, pero el proyecto no las exige para compilar.

---

## 8. Flujo de trabajo Git

Este repo es **nuevo** (sin historial del proyecto original). Recomendaciones:

- Usa `main` como rama principal.
- Trabaja en ramas cortas: `feat/...`, `fix/...`, `docs/...`.
- Haz commits atómicos con mensajes descriptivos.
- Ejecuta `./mvnw test` y `pnpm build` antes de hacer push.
- No subas `.env`, archivos de backups, ni `migration-client.csv` (todo está en
  `.gitignore`).

---

## 9. Herramientas útiles

- **Spring Boot DevTools** — reinicio automático (habilitado en `dev`).
- **Vite HMR** — recarga instantánea en el frontend.
- **TanStack Query Devtools** — inspección de queries mutaciones.
- **React Devtools** — inspección de componentes.

---

## 10. Solución de problemas

| Problema | Causa | Solución |
|---|---|---|
| `JwtFilter` 401 inesperado | Cookie no llega o Secure=true en HTTP | Revisa `COOKIE_SECURE` en `.env`. En localhost con HTTP deja `false`. |
| CORS bloqueado | Origen no permitido | Añade tu `http://localhost:5173` (o puerto usado) a `CORS_ALLOWED_ORIGINS`. |
| Orval falla al generar | Backend no está corriendo | Arranca `mvnw spring-boot:run` antes de `pnpm generate:api`. |
| Flyway falla al arrancar | Schema obsoleto/inconsistente | No uses `ddl-auto=create` en dev. Revisa migraciones V1–V10 o recrea la BD. |
| `mvnw` muy lento | Cache Maven vacío | Deja la primera ejecución: descargará dependencias (~10–15 min). |
| pnpm `frozen-lockfile` falla | `pnpm-lock.yaml` desactualizado | Actualiza el lockfile o corre `pnpm install` sin `--frozen-lockfile` localmente. |

---

## 11. Checklist antes de publicar

- [ ] `backend/.env` usa `JWT_SECRET` único (no el de demo).
- [ ] `MIGRACION_CLIENTES_AUTO=false`.
- [ ] Google OAuth / SMTP / pasarelas usan credenciales propias o se dejan vacíos.
- [ ] `pnpm build` compila sin errores.
- [ ] `./mvnw clean test` pasa (165/165).
- [ ] No hay secretos en el árbol ni en el índice: `git status` limpio.
- [ ] README refleja el estado actual.
- [ ] `LICENSE` correcta para la publicación prevista.
- [ ] Capturas añadidas en `docs/img/` (si aplica).
- [ ] `docker-compose.yml` solo referencia credenciales de ejemplo.
