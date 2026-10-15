import { customInstance } from "@/api/custom-instance";

export interface MisReservasHabitacion {
  id: number;
  numeroHabitacion: string;
  tipoHabitacion: string;
  precioPorNoche: number;
  cantidadOcupantes: number;
}

export interface MisReservasItem {
  id: number;
  codigoReserva: string;
  fechaCheckIn: string;
  fechaCheckOut: string;
  fechaCreacion: string;
  fechaExpiracionPago?: string | null;
  noches: number;
  estado: "PENDIENTE" | "PENDIENTE_PAGO" | "CONFIRMADA" | "EN_CURSO" | "FINALIZADA" | "CANCELADA" | "NO_SHOW";
  total: number;
  observaciones?: string | null;
  habitaciones: MisReservasHabitacion[];
}

export const misReservasApi = {
  listar: () => customInstance<MisReservasItem[]>({ url: "/api/reservas/mis-reservas", method: "GET" }),
  cancelar: (id: number) =>
    customInstance<MisReservasItem>({ url: `/api/reservas/mis-reservas/${id}/cancelar`, method: "POST" }),
  editarFechas: (id: number, fechaCheckIn: string, fechaCheckOut: string) =>
    customInstance<MisReservasItem>({
      url: `/api/reservas/mis-reservas/${id}/fechas`,
      method: "PUT",
      data: { fechaCheckIn, fechaCheckOut },
    }),
};
