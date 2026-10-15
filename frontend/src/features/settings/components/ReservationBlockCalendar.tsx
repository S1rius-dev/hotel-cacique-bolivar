import { useState } from "react";
import { format, addMonths, subMonths } from "date-fns";
import { es } from "date-fns/locale";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import {
  deriveReservationBlockState,
  type BlockState,
  type BookingBlock,
} from "../types";
import {
  getMonthGrid,
  findBlockForDate,
} from "../utils/blocks.utils";

interface Props {
  blocks: BookingBlock[];
  loading?: boolean;
  onDayClick: (date: Date) => void;
}

const WEEKDAYS = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"] as const;

const CELL_BG: Record<BlockState, string> = {
  ACTIVO: "bg-rose-500/20",
  PROXIMO: "bg-orange-500/20",
  FINALIZADO: "bg-violet-500/20",
  DESACTIVADO: "bg-zinc-500/15",
};

const MOTIVO_TEXT: Record<BlockState, string> = {
  ACTIVO: "text-rose-400",
  PROXIMO: "text-orange-400",
  FINALIZADO: "text-violet-400",
  DESACTIVADO: "text-zinc-400",
};

const DOT_CLASS: Record<BlockState, string> = {
  ACTIVO: "bg-rose-500",
  PROXIMO: "bg-orange-500",
  FINALIZADO: "bg-violet-500",
  DESACTIVADO: "bg-zinc-500",
};

const LEGEND: { label: string; state: BlockState }[] = [
  { label: "Bloqueo activo", state: "ACTIVO" },
  { label: "Próximo bloqueo", state: "PROXIMO" },
  { label: "Bloqueo finalizado", state: "FINALIZADO" },
  { label: "Bloqueo desactivado", state: "DESACTIVADO" },
];

export function ReservationBlockCalendar({ blocks, loading, onDayClick }: Props) {
  const [month, setMonth] = useState(() => new Date());
  const weeks = getMonthGrid(month);
  const title = format(month, "LLLL yyyy", { locale: es });

  if (loading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-8 w-40" />
        <div className="grid grid-cols-7 gap-[2px]">
          {Array.from({ length: 7 }).map((_, i) => (
            <Skeleton key={i} className="h-6" />
          ))}
        </div>
        {Array.from({ length: 5 }).map((_, row) => (
          <Skeleton key={row} className="grid h-10 grid-cols-7 gap-[2px]" />
        ))}
      </div>
    );
  }

  return (
    <div className="min-w-0">
      <div className="mb-3 flex items-center justify-between gap-2">
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={() => setMonth((m) => subMonths(m, 1))}
          className="cursor-pointer"
          aria-label="Mes anterior"
        >
          <ChevronLeft />
        </Button>
        <div className="flex items-center gap-2">
          <p className="text-sm font-semibold capitalize text-foreground">
            {title}
          </p>
          <Button
            variant="outline"
            size="xs"
            onClick={() => setMonth(new Date())}
            className="cursor-pointer"
          >
            Hoy
          </Button>
        </div>
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={() => setMonth((m) => addMonths(m, 1))}
          className="cursor-pointer"
          aria-label="Mes siguiente"
        >
          <ChevronRight />
        </Button>
      </div>

      <div className="mb-1 grid grid-cols-7 gap-[2px]">
        {WEEKDAYS.map((day) => (
          <div
            key={day}
            className="py-1 text-center text-[11px] font-medium text-muted-foreground"
          >
            {day}
          </div>
        ))}
      </div>

      <div className="space-y-1.5">
        {weeks.map((week, weekIndex) => {
          const dayStates = week.map((day) => {
            const block = findBlockForDate(blocks, day.date);
            const state = block ? deriveReservationBlockState(block) : null;
            return { ...day, block, state };
          });

          const weekMotivos: {
            startCol: number;
            span: number;
            motivo: string;
            state: BlockState;
          }[] = [];
          let i = 0;
          while (i < 7) {
            const b = dayStates[i].block;
            if (!b) { i++; continue; }
            const s = dayStates[i].state!;
            const startCol = i;
            let endCol = i;
            while (endCol + 1 < 7 && dayStates[endCol + 1].block?.id === b.id) {
              endCol++;
            }
            weekMotivos.push({
              startCol,
              span: endCol - startCol + 1,
              motivo: b.motivo,
              state: s,
            });
            i = endCol + 1;
          }

          return (
            <div key={weekIndex} className="min-w-0">
              <div className="grid grid-cols-7 gap-[2px]">
                {dayStates.map((day, colIndex) => {
                  const isStart = day.block && (
                    colIndex === 0 || dayStates[colIndex - 1].block?.id !== day.block.id
                  );
                  const isEnd = day.block && (
                    colIndex === 6 || dayStates[colIndex + 1].block?.id !== day.block.id
                  );
                  return (
                    <button
                      key={day.date.toISOString()}
                      type="button"
                      onClick={() => onDayClick(day.date)}
                      title={day.block ? `${day.block.motivo} · ${day.state}` : undefined}
                      className={cn(
                        "group flex min-w-0 items-center justify-center py-1 outline-none transition-colors focus-visible:ring-2 focus-visible:ring-ring/50",
                        !day.inMonth && "opacity-40",
                        day.state && CELL_BG[day.state],
                        day.state && isStart && "rounded-l-md",
                        day.state && isEnd && "rounded-r-md",
                      )}
                      aria-label={format(day.date, "PPPP", { locale: es })}
                    >
                      <span
                        className={cn(
                          "flex size-6 items-center justify-center rounded-md text-xs tabular-nums",
                          day.isToday
                            ? "bg-rose-800 font-semibold text-white"
                            : day.state
                              ? "font-medium text-foreground"
                              : "text-foreground group-hover:bg-muted",
                        )}
                      >
                        {format(day.date, "d")}
                      </span>
                    </button>
                  );
                })}
              </div>

              {weekMotivos.length > 0 && (
                <div className="grid grid-cols-7 gap-[2px]">
                  {weekMotivos.map((m) => (
                    <div
                      key={`${m.motivo}-${m.startCol}`}
                      className={cn(
                        "flex items-center justify-center px-0.5 pt-0.5 text-[9px] leading-tight",
                        MOTIVO_TEXT[m.state],
                      )}
                      style={{
                        gridColumn: `${m.startCol + 1} / ${m.startCol + 1 + m.span}`,
                      }}
                    >
                      <span className="truncate">{m.motivo}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>

      <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-1.5 border-t border-zinc-100 pt-3 dark:border-zinc-800">
        {LEGEND.map((item) => (
          <div key={item.state} className="flex items-center gap-1.5">
            <span className={cn("size-2 rounded-full", DOT_CLASS[item.state])} />
            <span className="text-xs text-muted-foreground">{item.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}