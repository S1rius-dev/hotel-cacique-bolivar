export const ESTADO_OPTIONS = [
  { value: "PENDIENTE", label: "Pendiente" },
  { value: "CONFIRMADA", label: "Confirmada" },
  { value: "EN_CURSO", label: "En curso" },
  { value: "FINALIZADA", label: "Finalizada" },
  { value: "CANCELADA", label: "Cancelada" },
  { value: "NO_SHOW", label: "No Llegó" },
];

export const ORIGEN_OPTIONS = [
  { value: "WEB", label: "Web" },
  { value: "RECEPCION", label: "Recepción" },
  { value: "TELEFONO", label: "Teléfono" },
  { value: "WHATSAPP", label: "WhatsApp" },
  { value: "OTRO", label: "Otro" },
];

export const DETALLE_ESTADO_STYLES: Record<
  string,
  { label: string; className: string }
> = {
  RESERVADA: {
    label: "Reservada",
    className:
      "bg-yellow-100 text-yellow-700 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-400 dark:border-yellow-800",
  },
  OCUPADA: {
    label: "Ocupada",
    className:
      "bg-green-100 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-400 dark:border-green-800",
  },
  LIBERADA: {
    label: "Liberada",
    className:
      "bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-900/30 dark:text-zinc-400 dark:border-zinc-800",
  },
  CANCELADA: {
    label: "Cancelada",
    className:
      "bg-red-100 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800",
  },
};
