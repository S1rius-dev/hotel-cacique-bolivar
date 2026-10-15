# Runbook: Copias de seguridad de MySQL

Procedimiento operativo del sistema de backups. La API administra el ciclo de vida.
La restauracion se expone **solo como clon a una base nueva**; el cambio de base en
produccion sigue siendo un procedimiento manual y fuera de banda (seccion 6).

## 1. Configuracion

Todo vive bajo `app.backup` y esta **deshabilitado por defecto**.

| Propiedad | Default | Que hace |
|---|---|---|
| `app.backup.enabled` | `false` | Interruptor maestro. En `false` la API responde 503 y no hay tareas programadas. |
| `app.backup.storage.dir` | `./backups` | Raiz del almacenamiento local. El panel la muestra en `rutaAlmacenamiento`. |
| `app.backup.mysqldump-path` | `mysqldump` | Ruta al ejecutable de volcado. En Windows suele no estar en PATH. |
| `app.backup.mysql-path` | `mysql` | Ruta al ejecutable cliente, usado para clonar. **Requerido** para el clon. |
| `app.backup.dump-extra-args` | `--single-transaction --routines --triggers --events --default-character-set=utf8mb4` | Flags del dump. No poner `--lock-tables`. |
| `app.backup.timeout-seconds` | `1800` | Corte por excedido del proceso de volcado. |
| `app.backup.clon.timeout-seconds` | `3600` | Corte del clon. Mayor que el del dump: importar escribe mas de lo que lee. |
| `app.backup.clon.default-prefix` | `hotel_restaurada_` | Prefijo que sugiere la UI. |
| `app.backup.schedule.enabled` | `false` | Registra las tareas cron. |
| `app.backup.schedule.cron` | `0 0 2 * * *` | Backup automatico. Zona fija en `America/Bogota`. |
| `app.backup.schedule.cleanup-cron` | `0 0 4 * * *` | Purga de retenidos. |
| `app.backup.schedule.cleanup-enabled` | `true` | Interruptor de la purga. |
| `app.backup.schedule.zone` | `America/Bogota` | Zona horaria de ambos cron. |
| `app.backup.retention.automatic-days` | `7` | Dias de retencion de automaticas. `-1` = nunca purgar. |
| `app.backup.retention.manual-days` | `-1` | Manuales se conservan por defecto. |

Variables de entorno equivalentes: `BACKUP_ENABLED`, `BACKUP_STORAGE_DIR`, `MYSQLDUMP_PATH`,
`MYSQL_PATH`, `BACKUP_TIMEOUT_SECONDS`, `BACKUP_CLON_TIMEOUT_SECONDS`, `BACKUP_CRON`,
`BACKUP_RETENTION_DAYS`, `BACKUP_RETENTION_MANUAL_DAYS`, `BACKUP_SCHEDULE_ENABLED`.

> En Windows **ni `mysqldump` ni `mysql` estan en el PATH** de una instalacion tipica. Sin
> `MYSQL_PATH` el volcado funciona pero el clon falla con un error de ejecucion.

## 2. Activacion en produccion

```powershell
$env:BACKUP_ENABLED="true"
$env:BACKUP_SCHEDULE_ENABLED="true"
$env:BACKUP_STORAGE_DIR="D:\hotel-backups"
$env:MYSQLDUMP_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
$env:MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
```

Requisitos previos:

1. `D:\hotel-backups` debe existir y tener espacio para al menos 8 copias automaticas.
2. El usuario del servicio de Windows debe tener permiso de escritura y lectura sobre ese directorio.
3. `MYSQLDUMP_PATH` y `MYSQL_PATH` deben apuntar a binarios de la **misma version** que el servidor MySQL.
4. El usuario MySQL de la aplicacion necesita `CREATE` y `DROP` para poder clonar.
5. Verificar permisos: solo el administrador del servidor debe leer los `.sql.gz`, contienen datos de clientes.

## 3. Verificacion tras activar

```powershell
# Dashboard: ultimo backup, totales, si hay uno en curso
curl.exe -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/backups/dashboard

# Backup manual bajo demanda
curl.exe -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/backups

# Listado
curl.exe -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/backups?size=20"
```

Un backup sano aparece con `estado=EXITOSO`, `tamanoBytes > 0` y `checksumSha256` de 64 hex.

## 4. API

Todos los endpoints requieren `ROLE_ADMIN`. Respuestas de error en espanol.

| Metodo | Ruta | Descripcion |
|---|---|---|
| `GET` | `/api/backups` | Listado paginado. Filtros opcionales `tipo` y `estado`. |
| `GET` | `/api/backups/{id}` | Detalle de un backup. |
| `GET` | `/api/backups/dashboard` | Resumen: ultimo exitoso, totales, si hay ejecucion en curso, proxima ejecucion, `rutaAlmacenamiento` y `baseDeDatosEnUso`. |
| `POST` | `/api/backups` | Crea un backup manual. Sincrono, devuelve el registro terminado. |
| `POST` | `/api/backups/{id}/verificar` | Recalcula el SHA-256 y lo compara con el registrado. Solo lectura. |
| `POST` | `/api/backups/{id}/clonar` | Restaura en una base nueva. Sincrono. Cuerpo: `{"baseDestino":"..."}`. |
| `GET` | `/api/backups/{id}/download` | Descarga el `.sql.gz` en streaming. |
| `DELETE` | `/api/backups/{id}` | Borra archivo y registro. |

Valores de `tipo`: `AUTOMATICO`, `MANUAL`. De `estado`: `EN_PROGRESO`, `EXITOSO`, `FALLIDO`.

## 5. Verificacion de integridad

```powershell
curl.exe -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/backups/2/verificar
```

```json
{
  "backupId": 2,
  "integro": true,
  "checksumEsperado": "30bd2e9ae55ef46a...",
  "checksumCalculado": "30bd2e9ae55ef46a...",
  "rutaArchivo": "manual/backup-manual-2026-09-26T18-13-22.sql.gz"
}
```

Un `integro=false` **no** es un error HTTP: responde 200 justamente para que la UI muestre el
detalle esperado frente al calculado. Un checksum discrepante significa que el archivo cambio
o se corrompio: **esa copia no es restaurable**.

> El checksum se calcula sobre el `.sql.gz` publicado, no sobre el SQL sin comprimir. Un
> `.sql.gz` que descomprima bien puede aun asi tener el checksum roto, y viceversa.

## 6. Restauracion

### 6.1 Clon a una base nueva (recomendado, desde la UI)

`Restaurar en base nueva` en el menu de acciones de cada copia. El backend:

1. Valida el nombre: solo `[A-Za-z0-9_]{1,64}`.
2. Rechaza el destino si coincide con `baseDeDatosEnUso`, sin importar mayusculas.
3. Verifica el SHA-256 del `.sql.gz` antes de tocar el servidor.
4. Crea la base con `CHARACTER SET utf8mb4` e importa el dump por stdin de `mysql`.
5. Si MySQL falla a mitad, **elimina la base destino**: un clon a medias parece restaurado
   sin serlo, que es peor que no tener nada.

La base en uso no se modifica en ningun momento. El dump no contiene `CREATE DATABASE`,
`USE`, `DROP DATABASE`, `GRANT`, `CREATE USER` ni `DEFINER`, por lo que importarlo es seguro
y las tablas caen en la base indicada por `--database`.

```powershell
curl.exe -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" `
  -d '{"baseDestino":"hotel_restaurada_2026_09_26"}' `
  http://localhost:8080/api/backups/2/clonar
```

Verificacion del clon:

```sql
SELECT COUNT(*) FROM reservas;
SELECT COUNT(*) FROM clientes;
SELECT COUNT(*) FROM pagos;
SHOW TABLES;
```

Los conteos deben coincidir con los de la base viva. Si no cuadran, conservar el dump
original y escalar; **no** cambiar la base de la aplicacion por el clon sin revisar.

Cuando el clon ya no se necesite:

```powershell
mysql.exe -h localhost -u root -e "DROP DATABASE hotel_restaurada_2026_09_26;"
```

Los clones **no** se registran en `backup_history` ni aparecen en el panel: son bases de
MySQL, no copias. Hay que llevar su propia cuenta de las que se crearon.

### 6.2 Cambio de base en produccion (fuera de banda)

> **La aplicacion debe estar detenida durante todo el procedimiento.** Reemplazar la base
> que la app tiene abierta significa cambiar el schema y los datos bajo las conexiones
> vivas de HikariCP y Hibernate, con bloqueos en metadata locks y cache de segundo nivel
> desalineado. El resultado tipico es un arranque caido.

```powershell
# 1. Verificar el clon antes de nada: es el punto de retorno
# 2. Detener la aplicacion
Stop-Service <NombreDelServicio>

# 3. Volcar la base actual por si el clon no resultara util
$env:MYSQL_PWD="clave"
mysqldump.exe -h localhost -u root --single-transaction --routines --triggers hotelcaciquebolivar > respaldo_previo.sql.gz

# 4. Recrear el schema e importar encima
mysql.exe -h localhost -u root -e "DROP DATABASE hotelcaciquebolivar; CREATE DATABASE hotelcaciquebolivar CHARACTER SET utf8mb4;"
mysql.exe -h localhost -u root hotelcaciquebolivar < respaldo_previo.sql.gz
```

Si el conteo no cuadra, **no** arrancar: conservar el dump original y escalar.

## 7. Diagnostico

| Sintoma | Causa probable | Accion |
|---|---|---|
| `ruta configurada` en el mensaje de error | Ruta no existe o no es ejecutable | Confirmar `Test-Path $env:MYSQLDUMP_PATH` (o `MYSQL_PATH` si falla el clon) |
| `No se pudo interpretar el JDBC url` | La app corre sobre H2 u otra base | Revisar `spring.datasource.url` del perfil activo |
| `mysqldump excedio el timeout de N segundos` | Dump lento o tabla bloqueada | Subir `BACKUP_TIMEOUT_SECONDS`; revisar bloqueos |
| `la importacion en 'X' excedio el timeout` | Clon lento | Subir `BACKUP_CLON_TIMEOUT_SECONDS` |
| `mysqldump termino con codigo 2` | Permisos o credenciales | Revisar el usuario MySQL de la app |
| `El sistema esta deshabilitado` | `BACKUP_ENABLED=false` | Activar la variable y reiniciar |
| `La copia N esta danada (SHA-256 esperado...)` | Archivo alterado o corrupto | No es restaurable. Descargar de nuevo o crear una copia nueva |
| `No se puede restaurar sobre 'X', que es la base en uso` | Destino igual a la base de la app | Elegir otro nombre. Es una proteccion, no un fallo |
| `Nombre de base de datos invalido` | Caracteres no admitidos | Solo letras, digitos y guion bajo |
| `.sql.gz` no restaura | Archivo truncado o corrupto | Correr `/verificar`; si el checksum no coincide, no es recuperable |
| `Ya hay una copia en ejecucion` | Backup anterior colgado | Revisar `estado=EN_PROGRESO`; se limpia solo tras `timeout-seconds` |

Registros con `estado=EN_PROGRESO` que superen el timeout se marcan `FALLIDO` automaticamente.
Si un backup quedo en `FALLIDO` pero el archivo existe, el archivo es parcial y **no** debe usarse.

## 8. Copias fuera del servidor

`rutaAlmacenamiento` indica donde viven los `.sql.gz` en el servidor actual. Una copia en el
mismo disco **no** protege de una perdida de disco, de un borrado accidental ni de un
ransomware. Copiar periodicamente a un destino externo es parte del procedimiento, no una
extras.

## 9. Rotacion de V3

`V3__create_backup_history.sql` ocupa la version 3 de Flyway. Si se implementa despues
`PAYMENT-SEPARATION-PLAN.md`, su migracion debe renumerarse a `V4__...` y el plan de pagos
actualizarse. Flyway no permite reutilizar ni modificar una version ya aplicada.

Si en el futuro se anade seguimiento de clones (que base se creo desde que copia y cuando),
esa migracion debe ser `V4__...`.
