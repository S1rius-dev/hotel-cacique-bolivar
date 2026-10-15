export interface BookingRoomType {
  id: number;
  nombre: string;
  descripcion: string;
  capacidadBase: number;
  capacidadMaxima: number;
  precioBase: number;
  precioPersonaExtra: number;
  precioNocheEfectivo?: number;
  precioPersonaExtraEfectivo?: number;
  totalEstimadoPorNoche?: number;
}

export interface BookingSelection {
  tipo: BookingRoomType;
  quantity: number;
}

export type OccupantConfig = Record<number, number[]>;
