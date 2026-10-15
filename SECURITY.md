# Política de seguridad

## Alcance

Este repositorio es una **demostración pública** del sistema de gestión hotelera
**Hotel Cacique Bolívar**. El código se publica con fines de portafolio y
evaluación técnica.

La demostración **no es un servicio en producción** y no debe desplegarse como
tal sin una revisión de seguridad previa. La información sensible (credenciales,
datos de clientes y de huéspedes, volcados de base de datos e infraestructura
privada) ha sido excluida deliberadamente del repositorio.

## Datos de demostración

- Los datos de contacto, el NIT y los identificadores que aparecen en el código
  son **ficticios** o han sido sustituidos por valores de ejemplo.
- No se incluye ningún volcado ni archivo con datos reales de clientes.
- Las cuentas de demostración (administrador y demás roles) usan datos ficticios
  definidos mediante variables de entorno locales.
- Las credenciales de los servicios externos (correo, pasarelas de pago, OAuth)
  se pasan por variables de entorno y **no** están en el repositorio. Los valores
  de ejemplo usan marcadores `CHANGEME`.

## Prácticas de seguridad del proyecto

- Autenticación **stateless** con JWT firmados (HMAC-SHA256) transportados en
  **cookies HttpOnly**.
- Rotación de *refresh token* y validación de expiración.
- Contraseñas almacenadas con **BCrypt**; nunca en texto plano.
- Autorización por roles (`ADMIN`, `RECEPCIONISTA`, `CLIENTE`) en la cadena de
  filtros de Spring Security.
- Validación de *fail-fast* de configuración: la aplicación no arranca si faltan
  secretos críticos en el perfil de producción (`ProdEnvironmentValidator`).
- CORS restringido por lista de orígenes configurable; por defecto solo el
  frontend local.
- Límite de tasa en las peticiones (`RateLimitFilter`).

## Cómo reportar una vulnerabilidad

Si encuentras un problema de seguridad, **no abras un issue público**. Escribe a
la dirección de contacto del autor indicando:

1. Descripción del problema y su impacto.
2. Pasos para reproducirlo.
3. Versión o commit afectado.
4. Cualquier evidencia o captura relevante.

Se atenderá el reporte lo antes posible y se dará crédito si el reportante lo
desea.

## Fuera de alcance

- Ataques que requieran acceso físico al equipo donde se ejecuta la demo.
- Ingeniería social.
- Vulnerabilidades en dependencias de terceros no explotables en esta demo
  (aun así, se agradece el aviso).
