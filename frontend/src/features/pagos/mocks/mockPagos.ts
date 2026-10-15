import type { PagoDashboard } from "../types/dashboard.types";

const now = new Date();
const iso = (h: number, m = 0) => {
  const d = new Date(now);
  d.setHours(h, m, 0, 0);
  return d.toISOString();
};
const isoDaysAgo = (days: number, h = 10) => {
  const d = new Date(now);
  d.setDate(d.getDate() - days);
  d.setHours(h, 0, 0, 0);
  return d.toISOString();
};

export const MOCK_PAGOS: PagoDashboard[] = [
  {
    id: "pay_001",
    reservaId: 101,
    clienteNombre: "Andrés Felipe Torres",
    habitacion: "Hab 204 · Sencilla",
    monto: 185000,
    canal: "bold",
    estado: "aprobada",
    createdAt: iso(9, 12),
    wompiTransactionId: "bold_9f3a2c1e-4b5d",
    paymentMethod: "Tarjeta Visa *4242",
    eventos: [
      { id: "e1", descripcion: "Transacción creada", timestamp: iso(9, 12) },
      { id: "e2", descripcion: "Enviada a Bold", timestamp: iso(9, 13), subtitulo: "Tarjeta Visa *4242" },
      { id: "e3", descripcion: "Aprobada por el banco", timestamp: iso(9, 14) },
    ],
  },
  {
    id: "pay_002",
    reservaId: 102,
    clienteNombre: "Laura Camila Ríos",
    habitacion: "Hab 101 · Pareja",
    monto: 420000,
    canal: "efectivo",
    estado: "aprobada",
    createdAt: iso(8, 30),
    recibidoPor: "Mariana López",
    numeroRecibo: "REC-0087",
    eventos: [
      { id: "e1", descripcion: "Pago recibido en caja", timestamp: iso(8, 30), subtitulo: "$420.000 COP" },
      { id: "e2", descripcion: "Registrado por Mariana López", timestamp: iso(8, 32) },
      { id: "e3", descripcion: "Recibo #REC-0087 generado", timestamp: iso(8, 33) },
    ],
  },
  {
    id: "pay_003",
    reservaId: 103,
    clienteNombre: "Carlos Andrés Muñoz",
    habitacion: "Reserva #B-892",
    monto: 210000,
    canal: "bold",
    estado: "pendiente",
    createdAt: iso(10, 45),
    wompiTransactionId: "bold_7a1e9d00-ff12",
    paymentMethod: "PSE · Bancolombia",
    eventos: [
      { id: "e1", descripcion: "Transacción creada", timestamp: iso(10, 45) },
      { id: "e2", descripcion: "Enviada a Bold", timestamp: iso(10, 46), subtitulo: "PSE · Bancolombia" },
    ],
  },
  {
    id: "pay_004",
    reservaId: 104,
    clienteNombre: "Valentina Gómez",
    habitacion: "Hab 305 · Familiar",
    monto: 95000,
    canal: "efectivo",
    estado: "aprobada",
    createdAt: iso(7, 15),
    recibidoPor: "Jorge Ramírez",
    numeroRecibo: "REC-0086",
    eventos: [
      { id: "e1", descripcion: "Pago recibido en caja", timestamp: iso(7, 15) },
      { id: "e2", descripcion: "Registrado por Jorge Ramírez", timestamp: iso(7, 16) },
      { id: "e3", descripcion: "Recibo #REC-0086 generado", timestamp: iso(7, 16) },
    ],
  },
  {
    id: "pay_005",
    reservaId: 105,
    clienteNombre: "Sofia Herrera",
    habitacion: "Hab 202 · Sencilla",
    monto: 185000,
    canal: "bold",
    estado: "rechazada",
    createdAt: isoDaysAgo(1, 16),
    wompiTransactionId: "bold_c3d4e5f6-0011",
    paymentMethod: "Nequi",
    motivoRechazo: "Fondos insuficientes",
    wompiStatusRaw: "DECLINED",
    eventos: [
      { id: "e1", descripcion: "Transacción creada", timestamp: isoDaysAgo(1, 16) },
      { id: "e2", descripcion: "Enviada a Bold", timestamp: isoDaysAgo(1, 16.5 as unknown as number) },
      { id: "e3", descripcion: "Rechazada: Fondos insuficientes", timestamp: isoDaysAgo(1, 17) },
    ],
  },
  {
    id: "pay_006",
    reservaId: 106,
    clienteNombre: "Diego Alejandro Ruiz",
    habitacion: "Hab 108 · Sencilla",
    monto: 320000,
    canal: "bold",
    estado: "aprobada",
    createdAt: iso(11, 20),
    wompiTransactionId: "bold_b2a1c3d4-9981",
    paymentMethod: "Tarjeta Mastercard *5541",
    eventos: [
      { id: "e1", descripcion: "Transacción creada", timestamp: iso(11, 20) },
      { id: "e2", descripcion: "Enviada a Bold", timestamp: iso(11, 21) },
      { id: "e3", descripcion: "Aprobada por el banco", timestamp: iso(11, 22) },
    ],
  },
  {
    id: "pay_007",
    reservaId: 107,
    clienteNombre: "Mariana Pardo",
    habitacion: "Reserva #A-441",
    monto: 150000,
    canal: "efectivo",
    estado: "pendiente",
    createdAt: iso(12, 5),
    recibidoPor: "Mariana López",
    numeroRecibo: "REC-0088",
    eventos: [
      { id: "e1", descripcion: "Pago recibido en caja", timestamp: iso(12, 5) },
      { id: "e2", descripcion: "Registrado por Mariana López", timestamp: iso(12, 6) },
    ],
  },
  {
    id: "pay_008",
    reservaId: 108,
    clienteNombre: "Julián Ortiz",
    habitacion: "Hab 303 · Familiar",
    monto: 275000,
    canal: "bold",
    estado: "aprobada",
    createdAt: iso(13, 40),
    wompiTransactionId: "bold_1234abcd-7777",
    paymentMethod: "PSE · Davivienda",
    eventos: [
      { id: "e1", descripcion: "Transacción creada", timestamp: iso(13, 40) },
      { id: "e2", descripcion: "Enviada a Bold", timestamp: iso(13, 41) },
      { id: "e3", descripcion: "Aprobada por el banco", timestamp: iso(13, 42) },
    ],
  },
];
