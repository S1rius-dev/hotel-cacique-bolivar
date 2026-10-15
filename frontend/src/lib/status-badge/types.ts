export interface StatusConfig {
  label: string;
  badgeClass: string;
  dotClass: string;
}

type ColorKey =
  | "EMERALD" | "VIOLET" | "ORANGE" | "ZINC" | "NEUTRAL"
  | "AMBER" | "SKY" | "BLUE" | "SLATE" | "RED" | "ROSE";

const COLOR_PRESETS: Record<ColorKey, Omit<StatusConfig, "label">> = {
  EMERALD: {
    badgeClass: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950 dark:text-emerald-400 dark:border-emerald-800",
    dotClass: "bg-emerald-500",
  },
  VIOLET: {
    badgeClass: "bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-950 dark:text-violet-400 dark:border-violet-800",
    dotClass: "bg-violet-500",
  },
  ORANGE: {
    badgeClass: "bg-orange-50 text-orange-700 border-orange-200 dark:bg-orange-950 dark:text-orange-400 dark:border-orange-800",
    dotClass: "bg-orange-500",
  },
  ZINC: {
    badgeClass: "bg-zinc-50 text-zinc-700 border-zinc-200 dark:bg-zinc-950 dark:text-zinc-400 dark:border-zinc-800",
    dotClass: "bg-zinc-500",
  },
  NEUTRAL: {
    badgeClass: "bg-neutral-50 text-neutral-700 border-neutral-200 dark:bg-neutral-950 dark:text-neutral-400 dark:border-neutral-800",
    dotClass: "bg-neutral-500",
  },
  AMBER: {
    badgeClass: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950 dark:text-amber-400 dark:border-amber-800",
    dotClass: "bg-amber-500",
  },
  SKY: {
    badgeClass: "bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950 dark:text-sky-400 dark:border-sky-800",
    dotClass: "bg-sky-500",
  },
  BLUE: {
    badgeClass: "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950 dark:text-blue-400 dark:border-blue-800",
    dotClass: "bg-blue-500",
  },
  SLATE: {
    badgeClass: "bg-slate-50 text-slate-700 border-slate-200 dark:bg-slate-950 dark:text-slate-400 dark:border-slate-800",
    dotClass: "bg-slate-500",
  },
  RED: {
    badgeClass: "bg-red-50 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-400 dark:border-red-800",
    dotClass: "bg-red-500",
  },
  ROSE: {
    badgeClass: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950 dark:text-rose-400 dark:border-rose-800",
    dotClass: "bg-rose-500",
  },
};

function buildConfig(color: ColorKey, label: string): StatusConfig {
  return { label, ...COLOR_PRESETS[color] };
}

export function statusConfig(label: string, color: ColorKey): StatusConfig {
  return buildConfig(color, label);
}
