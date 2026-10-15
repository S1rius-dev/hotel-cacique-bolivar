export type TipoPqrs =
  | "QUEJA"
  | "PETICION"
  | "RECLAMO"
  | "SUGERENCIA"
  | "FELICITACION"
  | "REPORTE_TECNICO";

export type EstadoPqrs =
  | "ABIERTA"
  | "EN_REVISION"
  | "RESPONDIDA"
  | "CERRADA"
  | "RECHAZADA";

export type PrioridadPqrs = "BAJA" | "MEDIA" | "ALTA" | "URGENTE";

export interface PQRSCreateRequest {
  tipo: TipoPqrs;
  asunto: string;
  descripcion: string;
  reservaId?: number;
  emailContacto?: string;
  telefonoContacto?: string;
  nombreContacto?: string;
}

export interface PQRSResponse {
  id: number;
  codigo: string;
  tipo: TipoPqrs;
  prioridad: PrioridadPqrs;
  estado: EstadoPqrs;
  asunto: string;
  descripcion: string;
  respuesta: string | null;
  fechaCreacion: string;
  fechaActualizacion: string | null;
  fechaRespuesta: string | null;
  usuarioId: number | null;
  usuarioEmail: string | null;
  usuarioNombre: string | null;
  reservaId: number | null;
  codigoReserva: string | null;
  emailContacto: string | null;
  telefonoContacto: string | null;
  nombreContacto: string | null;
}

export interface PQRSTrackingResponse {
  codigo: string;
  tipo: TipoPqrs;
  prioridad: PrioridadPqrs;
  asunto: string;
  descripcion: string;
  estado: EstadoPqrs;
  fechaCreacion: string;
  respuesta: string | null;
  fechaRespuesta: string | null;
  nombreContacto: string | null;
}
