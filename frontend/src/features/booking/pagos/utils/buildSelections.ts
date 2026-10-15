import type { TipoHabitacionPublicResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { BookingRoomType, BookingSelection } from "@/features/booking/types";

export interface HabitacionTipoRef {
  tipo?: string;
  tipoHabitacion?: string;
}

function normalizeNombre(value?: string | null): string {
  return (value ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

function toBookingRoomType(tipo: TipoHabitacionPublicResponse): BookingRoomType {
  return {
    id: tipo.id ?? 0,
    nombre: tipo.nombre ?? "",
    descripcion: tipo.descripcion ?? "",
    capacidadBase: tipo.capacidadBase ?? 0,
    capacidadMaxima: tipo.capacidadMaxima ?? 0,
    precioBase: tipo.precioBase ?? 0,
    precioPersonaExtra: tipo.precioPersonaExtra ?? 0,
  };
}

export function buildBookingSelections(
  habitaciones: HabitacionTipoRef[] | undefined,
  tipos: TipoHabitacionPublicResponse[] | undefined,
): BookingSelection[] {
  if (!habitaciones?.length || !tipos?.length) return [];

  const porNombre = new Map<string, TipoHabitacionPublicResponse>();
  for (const tipo of tipos) {
    const key = normalizeNombre(tipo.nombre);
    if (key && !porNombre.has(key)) porNombre.set(key, tipo);
  }

  const agrupado = new Map<number, BookingSelection>();
  for (const habitacion of habitaciones) {
    const match = porNombre.get(normalizeNombre(habitacion.tipo ?? habitacion.tipoHabitacion));
    if (!match?.id) continue;
    const roomType = toBookingRoomType(match);
    const actual = agrupado.get(roomType.id);
    if (actual) {
      actual.quantity += 1;
    } else {
      agrupado.set(roomType.id, { tipo: roomType, quantity: 1 });
    }
  }

  return [...agrupado.values()];
}
