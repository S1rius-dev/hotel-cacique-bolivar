import type { HuespedRegistroDTOEstadoDetalle } from "@/api/generated/openAPIDefinition.schemas";

export type GuestStatus = HuespedRegistroDTOEstadoDetalle;

export interface GuestFilters {
  fechaDesde?: string;
  fechaHasta?: string;
  habitacion?: string;
  tipoHabitacionId?: number;
  busqueda?: string;
  estado?: string;
}
