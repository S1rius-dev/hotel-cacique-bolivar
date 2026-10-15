import { CalendarClock, CalendarX2, ShieldAlert } from "lucide-react";
import type { BlockStats } from "../utils/blocks.utils";

interface Props {
  stats: BlockStats;
}

interface StatItem {
  key: keyof BlockStats;
  title: string;
  description: string;
  icon: typeof ShieldAlert;
  iconClass: string;
  accentClass: string;
}

const ITEMS: StatItem[] = [
  {
    key: "active",
    title: "Bloqueo activo",
    description: "Actualmente",
    icon: ShieldAlert,
    iconClass:
      "bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400",
    accentClass: "text-rose-700 dark:text-rose-400",
  },
  {
    key: "upcoming",
    title: "Próximo bloqueo",
    description: "En los próximos 30 días",
    icon: CalendarClock,
    iconClass:
      "bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400",
    accentClass: "text-orange-700 dark:text-orange-400",
  },
  {
    key: "endedThisMonth",
    title: "Bloqueos finalizados",
    description: "Este mes",
    icon: CalendarX2,
    iconClass:
      "bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400",
    accentClass: "text-violet-700 dark:text-violet-400",
  },
];

export function OnlineReservationStats({ stats }: Props) {
  return (
    <div className="grid grid-cols-3 gap-3 max-md:grid-cols-3">
      {ITEMS.map((item) => {
        const Icon = item.icon;
        return (
          <div
            key={item.key}
            className="flex flex-col gap-2 rounded-xl border border-zinc-200 bg-card p-3 shadow-sm dark:border-zinc-800"
          >
            <div
              className={`flex size-8 shrink-0 items-center justify-center rounded-lg ${item.iconClass}`}
            >
              <Icon className="size-4" />
            </div>
            <div className="min-w-0">
              <p className={`text-2xl font-semibold leading-none ${item.accentClass}`}>
                {stats[item.key]}
              </p>
              <p className="mt-1.5 truncate text-xs font-medium text-foreground">
                {item.title}
              </p>
              <p className="truncate text-[11px] text-muted-foreground">
                {item.description}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}