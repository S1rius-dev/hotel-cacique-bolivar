export const ESTADO_HABITACION_STYLES: Record<
  string,
  { dot: string; label: string; className: string; glow: string }
> = {
  DISPONIBLE: {
    dot: "bg-emerald-500",
    label: "Disponible",
    className:
      "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800",
    glow:
      "border-emerald-500/10 dark:border-emerald-500/20 shadow-[0_0_18px_rgba(16,185,129,0.06)] dark:shadow-[0_0_18px_rgba(16,185,129,0.12)] hover:shadow-[0_0_25px_rgba(16,185,129,0.12)] dark:hover:shadow-[0_0_25px_rgba(16,185,129,0.2)]",
  },
  RESERVADA: {
    dot: "bg-purple-500",
    label: "Reservada",
    className:
      "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400 border-purple-200 dark:border-purple-800",
    glow:
      "border-purple-500/10 dark:border-purple-500/20 shadow-[0_0_18px_rgba(168,85,247,0.06)] dark:shadow-[0_0_18px_rgba(168,85,247,0.12)] hover:shadow-[0_0_25px_rgba(168,85,247,0.12)] dark:hover:shadow-[0_0_25px_rgba(168,85,247,0.2)]",
  },
  OCUPADA: {
    dot: "bg-amber-500",
    label: "Ocupada",
    className:
      "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 border-amber-200 dark:border-amber-800",
    glow:
      "border-amber-500/10 dark:border-amber-500/20 shadow-[0_0_18px_rgba(245,158,11,0.06)] dark:shadow-[0_0_18px_rgba(245,158,11,0.12)] hover:shadow-[0_0_25px_rgba(245,158,11,0.12)] dark:hover:shadow-[0_0_25px_rgba(245,158,11,0.2)]",
  },
  EN_LIMPIEZA: {
    dot: "bg-sky-500",
    label: "En limpieza",
    className:
      "bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-400 border-sky-200 dark:border-sky-800",
    glow:
      "border-sky-500/10 dark:border-sky-500/20 shadow-[0_0_18px_rgba(14,165,233,0.06)] dark:shadow-[0_0_18px_rgba(14,165,233,0.12)] hover:shadow-[0_0_25px_rgba(14,165,233,0.12)] dark:hover:shadow-[0_0_25px_rgba(14,165,233,0.2)]",
  },
  MANTENIMIENTO: {
    dot: "bg-zinc-400",
    label: "Mantenimiento",
    className:
      "bg-zinc-100 text-zinc-600 dark:bg-zinc-900/30 dark:text-zinc-400 border-zinc-200 dark:border-zinc-800",
    glow:
      "border-zinc-400/10 dark:border-zinc-400/20 shadow-[0_0_18px_rgba(161,161,170,0.06)] dark:shadow-[0_0_18px_rgba(161,161,170,0.12)] hover:shadow-[0_0_25px_rgba(161,161,170,0.12)] dark:hover:shadow-[0_0_25px_rgba(161,161,170,0.2)]",
  },
  INHABILITADA: {
    dot: "bg-red-500",
    label: "Inhabilitada",
    className:
      "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400 border-red-200 dark:border-red-800",
    glow:
      "border-red-500/10 dark:border-red-500/20 shadow-[0_0_18px_rgba(239,68,68,0.06)] dark:shadow-[0_0_18px_rgba(239,68,68,0.12)] hover:shadow-[0_0_25px_rgba(239,68,68,0.12)] dark:hover:shadow-[0_0_25px_rgba(239,68,68,0.2)]",
  },
};

export const TIPO_ALERTA_LABELS: Record<string, string> = {
  ECONOMICO: "Pago pendiente",
  HURTO: "Hurto",
  CONFLICTIVO: "Conflictivo",
  FUMADOR: "Fumador",
  DANOS: "Daño habitación",
};
