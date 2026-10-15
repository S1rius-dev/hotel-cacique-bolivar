# Plan de Integración Wompi — Pasarela de Pago

Basado en el análisis del código existente y la documentación oficial de Wompi API.

---

## Estado actual

| Componente | Estado |
|---|---|
| `Pago` entity | ✅ Existe con campos: `codigoPago`, `monto`, `tipoPago`, `metodoPago`, `estado`, `referenciaExterna` |
| `PagoService.registrar()` | ✅ Crea pagos como `COMPLETADO` inmediatamente (flujo manual de recepción) |
| `MetodoPago` enum | ✅ Tiene: `EFECTIVO`, `TARJETA_CREDITO`, `TARJETA_DEBITO`, `TRANSFERENCIA`, `CHEQUE`, `OTRO` |
| `EstadoPago` enum | ✅ Tiene: `COMPLETADO`, `PENDIENTE`, `REEMBOLSADO`, `CANCELADO` |
| Integración con pasarela | ❌ Ninguna — no hay dependencias, ni clientes HTTP, ni webhooks |
| `PagoController` | ✅ Endpoints CRUD internos en `/api/pagos` |
| Llamadas externas a Wompi | ❌ No existe |

---

## Arquitectura propuesta

```
gateway/
├── config/
│   └── WompiProperties.java              (configuración: llaves, URLs)
├── client/
│   └── WompiClient.java                  (RestClient a la API de Wompi)
└── dto/
    ├── WompiTransactionRequest.java      (request para POST /transactions)
    ├── WompiTransactionResponse.java     (response de Wompi)
    ├── WompiWebhookEvent.java            (payload del webhook entrante)
    └── WompiAcceptanceToken.java         (token de aceptación)

webhook/
└── WompiWebhookController.java          (POST /api/webhooks/wompi)
```

---

## Archivos a modificar

| Archivo | Cambio |
|---|---|
| `pom.xml` | Agregar `spring-boot-starter-webflux` (para `WebClient` o usar `RestClient` de Spring 6) |
| `application.yml` | Agregar sección `wompi:` con llaves y URLs |
| `.env` | Agregar `WOMPI_PUBLIC_KEY`, `WOMPI_PRIVATE_KEY`, `WOMPI_WEBHOOK_SECRET` |
| `MetodoPago.java` | Agregar `WOMPI` al enum |
| `Pago.java` | Agregar `transactionId` String (ID de transacción Wompi) |
| `PagoServiceImpl.java` | Nuevo método `iniciarPagoWompi()` que crea transacción en Wompi, guarda Pago como `PENDIENTE` |

## Archivos nuevos

| Archivo | Propósito |
|---|---|
| `gateway/config/WompiProperties.java` | `@ConfigurationProperties(prefix = "wompi")` — inyecta `public-key`, `private-key`, `webhook-secret`, `url` |
| `gateway/client/WompiClient.java` | Cliente REST vía `RestClient` para `POST /v1/transactions`, `GET /v1/transactions/{id}`, `GET /v1/merchants/{publicKey}` (acceptance token) |
| `gateway/dto/*` | Records que mapean el request/response de la API Wompi |
| `webhook/WompiWebhookController.java` | `POST /api/webhooks/wompi` — recibe notificaciones, actualiza estado de Pago, audit log |
| `service/pago/PagoWompiService.java` + impl | Orquestador: crea transacción en Wompi + guarda Pago local |
| `dto/pago/PagoWompiRequest.java` | DTO público: `{ reservaId, metodoPago (CARD/NEQUI/PSE), redirectUrl }` |
| `dto/pago/PagoWompiResponse.java` | Respuesta: `{ pagoId, wompiTransactionId, status, redirectUrl }` |

---

## Flujo completo (Customer → Wompi → Webhook)

```
FRONTEND                          BACKEND                         WOMPI
   │                                 │                              │
   │ 1. Crea reserva                 │                              │
   │ POST /api/public/reservas ──────>                              │
   │                                 │                              │
   │ 2. Inicia pago                  │                              │
   │ POST /api/pagos/wompi/iniciar ──>                              │
   │  { reservaId, redirectUrl }     │                              │
   │                                 │                              │
   │                          ┌──────┴──────┐                       │
   │                          │ 1. Obtiene   │                      │
   │                          │ acceptance   │                      │
   │                          │ token        │──────────────────────>│
   │                          │              │<──────────────────────│
   │                          │ 2. Crea      │                      │
   │                          │ transacción  │──────────────────────>│
   │                          │              │<──────────────────────│
   │                          │ 3. Guarda    │                      │
   │                          │ Pago         │                      │
   │                          │ PENDIENTE    │                      │
   │                          └──────────────┘                      │
   │                                 │                              │
   │ <── { transactionId,           │                              │
   │       redirectUrl }            │                              │
   │                                 │                              │
   │ 3. Redirige a Wompi ────────────┼─────────────────────────────>│
   │    (o abre widget)              │                              │
   │                                 │  ┌─── 4. Webhook ────┐       │
   │                                 │  │ POST /api/webhooks │       │
   │                                 │  │ /wompi             │<──────│
   │                                 │  │ { event:           │       │
   │                                 │  │ transaction.      │       │
   │                                 │  │ updated, data: {   │       │
   │                                 │  │   transaction {    │       │
   │                                 │  │     id, status,    │       │
   │                                 │  │     reference }    │       │
   │                                 │  │ } }                │       │
   │                                 │  └────────────────────┘       │
   │                          ┌──────┴──────┐                       │
   │                          │ 1. Busca Pago│                      │
   │                          │ por reference│                      │
   │                          │ 2. Actualiza │                      │
   │                          │ estado       │                      │
   │                          │ 3. Audit log │                      │
   │                          │ 4. SSE event │                      │
   │                          └──────────────┘                      │
```

---

## Contrato Wompi (resumen)

### Crear transacción `POST /v1/transactions`

```json
{
  "amount_in_cents": 50000,
  "currency": "COP",
  "customer_email": "cliente@email.com",
  "payment_method": { "type": "CARD", "token": "tok_xxx", "installments": 1 },
  "reference": "RES-2026-XXXXXXXX",
  "acceptance_token": "eyJhbGci...",
  "signature": "sha256(amount + reference + privateKey)",
  "redirect_url": "https://hotel.com/pago/exito"
}
```

### Respuesta

```json
{
  "data": {
    "id": "1292-1602113476-10985",
    "status": "PENDING",
    "reference": "RES-2026-XXXXXXXX",
    "payment_method_type": "CARD"
  }
}
```

### Webhook `POST /api/webhooks/wompi`

(configurado en panel Wompi)

```json
{
  "event": "transaction.updated",
  "data": {
    "transaction": { "id": "...", "status": "APPROVED", "reference": "RES-..." }
  }
}
```

> **Verificar firma del webhook:** SHA256 del body + `webhook-secret` configurado.

---

## Decisiones de arquitectura

| Decisión | Razón |
|---|---|
| `WompiClient` separado del `PagoServiceImpl` | `PagoServiceImpl` maneja el flujo manual (recepción), el cliente Wompi es una capa nueva |
| `PagoWompiService` como orquestador | Coordina acceptance token + creación de transacción + persistencia, sin acoplar |
| `MetodoPago.WOMPI` | No reemplaza los existentes, solo agrega el nuevo origen |
| Webhook como mecanismo principal | Wompi recomienda webhooks sobre polling para conocer el estado final |
| `reference` = `codigoReserva` | Permite lookup bidireccional sin tabla de mapeo extra |
| `RestClient` (Spring 6) | Nativo en Spring Boot 4, sin dependencia extra pesada como WebFlux |

---

## Pendiente para implementación

- [ ] Agregar `spring-boot-starter-webflux` a `pom.xml`
- [ ] Crear `WompiProperties.java`
- [ ] Crear DTOs de Wompi: `WompiTransactionRequest`, `WompiTransactionResponse`, `WompiWebhookEvent`, `WompiAcceptanceToken`
- [ ] Crear `WompiClient.java`
- [ ] Agregar `WOMPI` a `MetodoPago.java`
- [ ] Agregar `transactionId` a `Pago.java`
- [ ] Crear `PagoWompiRequest.java` y `PagoWompiResponse.java`
- [ ] Crear `PagoWompiService.java` + impl
- [ ] Agregar `iniciarPagoWompi` a `PagoService.java`
- [ ] Crear `WompiWebhookController.java`
- [ ] Actualizar `application.yml` con sección `wompi:`
- [ ] Verificar compilación
