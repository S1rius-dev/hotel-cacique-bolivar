import {
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  eachDayOfInterval,
  isSameMonth,
  isToday,
} from "date-fns";
import {
  isBlockActive,
  deriveReservationBlockState,
  type BlockState,
  type BookingBlock,
} from "../types";

export interface CalendarDay {
  date: Date;
  inMonth: boolean;
  isToday: boolean;
}

export function getMonthGrid(month: Date): CalendarDay[][] {
  const first = startOfMonth(month);
  const last = endOfMonth(month);
  const gridStart = startOfWeek(first, { weekStartsOn: 1 });
  const gridEnd = endOfWeek(last, { weekStartsOn: 1 });

  const days = eachDayOfInterval({ start: gridStart, end: gridEnd });
  const weeks: CalendarDay[][] = [];
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(
      days.slice(i, i + 7).map((date) => ({
        date,
        inMonth: isSameMonth(date, month),
        isToday: isToday(date),
      })),
    );
  }
  return weeks;
}

export function findBlockForDate(
  blocks: BookingBlock[],
  date: Date,
): BookingBlock | null {
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  let best: BookingBlock | null = null;
  let bestPriority = Number.POSITIVE_INFINITY;
  for (const block of blocks) {
    if (!coversDate(block, target)) continue;
    const priority = STATE_PRIORITY[deriveReservationBlockState(block)];
    if (priority < bestPriority) {
      best = block;
      bestPriority = priority;
    }
  }
  return best;
}

const STATE_PRIORITY: Record<BlockState, number> = {
  ACTIVO: 0,
  PROXIMO: 1,
  FINALIZADO: 2,
  DESACTIVADO: 3,
};

function coversDate(block: BookingBlock, date: Date): boolean {
  const inicio = toLocalDate(block.fechaInicio);
  const fin = toLocalDate(block.fechaFin);
  return date.getTime() >= inicio.getTime() && date.getTime() <= fin.getTime();
}

function toLocalDate(value: string): Date {
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [y, m, d] = value.split("-").map(Number);
    return new Date(y, m - 1, d);
  }
  return new Date(value);
}

export interface BlockStats {
  active: number;
  upcoming: number;
  endedThisMonth: number;
}

export function computeBlockStats(
  blocks: BookingBlock[],
  now: Date = new Date(),
): BlockStats {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const in30Days = new Date(today);
  in30Days.setDate(in30Days.getDate() + 30);

  let active = 0;
  let upcoming = 0;
  let endedThisMonth = 0;

  for (const block of blocks) {
    if (!block.activo) continue;
    if (isBlockActive(block, today)) {
      active += 1;
    } else {
      const inicio = toLocalDate(block.fechaInicio);
      if (inicio.getTime() > today.getTime() && inicio.getTime() <= in30Days.getTime()) {
        upcoming += 1;
      }
    }
    const fin = toLocalDate(block.fechaFin);
    if (fin.getTime() < today.getTime() && fin.getMonth() === today.getMonth() && fin.getFullYear() === today.getFullYear()) {
      endedThisMonth += 1;
    }
  }

  return { active, upcoming, endedThisMonth };
}