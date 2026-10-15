import type { TipoPqrs } from "../types";
import {
  Frown,
  FileText,
  AlertTriangle,
  Lightbulb,
  Sparkles,
  Wrench,
  type LucideIcon,
} from "lucide-react";

export interface TipoPqrsConfig {
  value: TipoPqrs;
  label: string;
  subtitle: string;
  icon: LucideIcon;
  color: string;
  bgColor: string;
  ringColor: string;
  borderColor: string;
}

export const TIPOS_PQRS: TipoPqrsConfig[] = [
  {
    value: "PETICION",
    label: "Petición",
    subtitle: "Solicita información o un servicio",
    icon: FileText,
    color: "text-blue-600",
    bgColor: "bg-blue-100",
    ringColor: "ring-blue-200",
    borderColor: "border-l-blue-500",
  },
  {
    value: "QUEJA",
    label: "Queja",
    subtitle: "Expresa tu inconformidad",
    icon: Frown,
    color: "text-rose-600",
    bgColor: "bg-rose-100",
    ringColor: "ring-rose-200",
    borderColor: "border-l-rose-500",
  },
  {
    value: "RECLAMO",
    label: "Reclamo",
    subtitle: "Exige una solución",
    icon: AlertTriangle,
    color: "text-amber-600",
    bgColor: "bg-amber-100",
    ringColor: "ring-amber-200",
    borderColor: "border-l-amber-500",
  },
  {
    value: "SUGERENCIA",
    label: "Sugerencia",
    subtitle: "Comparte una idea para mejorar",
    icon: Lightbulb,
    color: "text-emerald-600",
    bgColor: "bg-emerald-100",
    ringColor: "ring-emerald-200",
    borderColor: "border-l-emerald-500",
  },
  {
    value: "FELICITACION",
    label: "Felicitación",
    subtitle: "Reconoce un buen servicio",
    icon: Sparkles,
    color: "text-violet-600",
    bgColor: "bg-violet-100",
    ringColor: "ring-violet-200",
    borderColor: "border-l-violet-500",
  },
  {
    value: "REPORTE_TECNICO",
    label: "Reporte Técnico",
    subtitle: "Notifica una falla o error en la web o sistema",
    icon: Wrench,
    color: "text-stone-600",
    bgColor: "bg-stone-100",
    ringColor: "ring-stone-200",
    borderColor: "border-l-stone-500",
  },
];

export const ESTADOS_PQRS: Record<
  string,
  { label: string; descripcion: string }
> = {
  ABIERTA: {
    label: "Abierta",
    descripcion: "Hemos recibido tu solicitud",
  },
  EN_REVISION: {
    label: "En Revisión",
    descripcion: "Estamos analizando tu caso",
  },
  RESPONDIDA: {
    label: "Respondida",
    descripcion: "Hemos dado respuesta a tu solicitud",
  },
  CERRADA: {
    label: "Cerrada",
    descripcion: "Este caso ha sido cerrado",
  },
  RECHAZADA: {
    label: "Rechazada",
    descripcion: "Tu solicitud no ha sido aprobada",
  },
};
