export const ESTADO_COLORS: Record<string, string> = {
  DISPONIBLE: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400",
  RESERVADA: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400",
  OCUPADA: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400",
  EN_LIMPIEZA: "bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-400",
  MANTENIMIENTO: "bg-zinc-100 text-zinc-600 dark:bg-zinc-900/30 dark:text-zinc-400",
  INHABILITADA: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400",
};

export const ESTADO_LABELS: Record<string, string> = {
  DISPONIBLE: "Disponible",
  RESERVADA: "Reservada",
  OCUPADA: "Ocupada",
  EN_LIMPIEZA: "En limpieza",
  MANTENIMIENTO: "Mantenimiento",
  INHABILITADA: "Inhabilitada",
};

export const ESTADO_FILTER_OPTIONS = [
  { value: "all", label: "Todas" },
  { value: "DISPONIBLE", label: "Disponibles" },
  { value: "RESERVADA", label: "Reservadas" },
  { value: "OCUPADA", label: "Ocupadas" },
  { value: "EN_LIMPIEZA", label: "En limpieza" },
  { value: "MANTENIMIENTO", label: "Mantenimiento" },
];