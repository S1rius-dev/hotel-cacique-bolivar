import { cn } from "@/lib/utils";
import type { StatusConfig } from "@/lib/status-badge/types";

const BOOLEAN_STATUS: Record<string, StatusConfig> = {
  true: {
    label: "Activo",
    badgeClass:
      "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950 dark:text-emerald-400 dark:border-emerald-800",
    dotClass: "bg-emerald-500",
  },
  false: {
    label: "Inactivo",
    badgeClass:
      "bg-red-50 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-400 dark:border-red-800",
    dotClass: "bg-red-500",
  },
};

const FALLBACK: StatusConfig = {
  label: "",
  badgeClass:
    "bg-zinc-50 text-zinc-700 border-zinc-200 dark:bg-zinc-950 dark:text-zinc-400 dark:border-zinc-800",
  dotClass: "bg-zinc-500",
};

interface StatusBadgeProps {
  config?: StatusConfig;
  status?: string;
  configMap?: Record<string, StatusConfig>;
  active?: boolean;
  className?: string;
}

export function StatusBadge({ config, status, configMap, active, className }: StatusBadgeProps) {
  let resolved: StatusConfig | undefined;

  if (config) {
    resolved = config;
  } else if (status && configMap) {
    resolved = configMap[status];
  } else if (active !== undefined) {
    resolved = BOOLEAN_STATUS[String(active)];
  }

  if (!resolved) {
    resolved = { ...FALLBACK, label: status ?? "—" };
  }

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium border",
        resolved.badgeClass,
        className,
      )}>
      <span className={cn("w-1.5 h-1.5 rounded-full shrink-0", resolved.dotClass)} />
      {resolved.label}
    </span>
  );
}
