import type { NotificacionResponseTipo } from "@/api/generated/openAPIDefinition.schemas";

export type NotificacionTipo = NotificacionResponseTipo;

export interface Notificacion {
  id: number;
  tipo: NotificacionTipo;
  mensaje: string;
  origen: string | null;
  origenNombre: string | null;
  detalles: Record<string, unknown> | null;
  referenciaId: number | null;
  referenciaTipo: string | null;
  leida: boolean;
  fechaCreacion: string;
  fechaLectura: string | null;
}
