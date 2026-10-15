/**
 * El backend modela el canal online con la clave de la pasarela que procesó el
 * pago (wompi, bold, mercado_pago, ...), por lo que el conjunto es abierto.
 * "efectivo" es el único valor fijo.
 */
export type Canal = "efectivo" | (string & {});
export type EstadoPago = "aprobada" | "pendiente" | "rechazada";

export interface PagoEvento {
  id: string;
  descripcion: string;
  timestamp: string; // ISO
  subtitulo?: string;
}

export interface PagoDashboard {
  id: string;
  /** Id numerico del backend. Ausente en los mocks, que no tienen trazabilidad. */
  pagoId?: number;
  reservaId: number;
  clienteNombre: string;
  clienteId?: number;
  habitacion: string; // ej "Hab 204 · Sencilla" o "Reserva #A19F"
  monto: number;
  canal: Canal;
  estado: EstadoPago;
  createdAt: string; // ISO
  // wompi
  wompiTransactionId?: string;
  paymentMethod?: string; // Tarjeta Visa *4242, PSE Bancolombia, Nequi
  wompiStatusRaw?: string;
  motivoRechazo?: string;
  // efectivo
  recibidoPor?: string; // nombre empleado
  recibidoPorId?: number;
  numeroRecibo?: string; // REC-0001
  // timeline auditable
  eventos: PagoEvento[];
}

export interface PagosMetrics {
  recaudadoHoy: number;
  wompi: { monto: number; count: number };
  efectivo: { monto: number; count: number };
  efectivoRecepcion: { monto: number; count: number };
  efectivoTienda: { monto: number; count: number };
  pendientes: number;
}
