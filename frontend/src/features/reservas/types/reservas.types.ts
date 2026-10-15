export interface SelectedRoom {
  numero: string;
  tipo: string;
  capacidad: number;
  precio: number;
  estado?: string;
  descripcion?: string;
  amenities?: string[];
  id?: number;
  tipoId?: number;
  adultos: number;
  ninos: number;
  /** Congela el modo de precio del detalle desde la creación. Sin valor = sin IVA. */
  precioIncluyeIva?: boolean;
}

export interface HuespedForm {
  nombre: string;
  documento: string;
}

export interface HabitacionHuespedForm {
  habitacion: {
    numero: string;
    tipo: string;
    capacidad: number;
    id?: number;
    precio?: number;
    adultos: number;
    ninos: number;
    precioIncluyeIva?: boolean;
  };
  huespedes: HuespedForm[];
}

export interface SelectedRoomSummary {
  numero: string;
  tipo: string;
  precio: number;
  adultos: number;
  ninos: number;
  precioIncluyeIva?: boolean;
}
