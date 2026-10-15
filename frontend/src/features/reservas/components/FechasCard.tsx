"use client";

import { CalendarIcon, MoonIcon } from "lucide-react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import { es } from "date-fns/locale";
import { useState, useMemo } from "react";
import { formatDateDayMonth } from "@/utils/date";

interface FechasBarProps {
  filtros: { checkIn: string; checkOut: string };
  onChange: (filtros: { checkIn: string; checkOut: string }) => void;
  noches: number;
}

const disabledDays = (d: Date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return d < today;
};

const FechasBar: React.FC<FechasBarProps> = ({ filtros, onChange, noches }) => {
  const [openCheckin, setOpenCheckin] = useState(false);
  const [openCheckout, setOpenCheckout] = useState(false);

  const checkIn = useMemo(
    () =>
      filtros.checkIn ? new Date(filtros.checkIn + "T00:00:00") : undefined,
    [filtros.checkIn],
  );
  const checkOut = useMemo(
    () =>
      filtros.checkOut ? new Date(filtros.checkOut + "T00:00:00") : undefined,
    [filtros.checkOut],
  );

  const disabledCheckout = useMemo(() => {
    return (d: Date) => {
      if (disabledDays(d)) return true;
      if (checkIn) return d <= checkIn;
      return false;
    };
  }, [checkIn]);

  const handleSelectCheckin = (d: Date | undefined) => {
    if (!d) return;
    const newCheckin = format(d, "yyyy-MM-dd");
    if (checkOut && d >= checkOut) {
      onChange({ checkIn: newCheckin, checkOut: "" });
    } else {
      onChange({ checkIn: newCheckin, checkOut: filtros.checkOut });
    }
    setOpenCheckin(false);
    setOpenCheckout(true);
  };

  const handleSelectCheckout = (d: Date | undefined) => {
    if (!d) return;
    onChange({
      checkIn: filtros.checkIn,
      checkOut: format(d, "yyyy-MM-dd"),
    });
    setOpenCheckout(false);
  };

  return (
    <div className="flex items-center gap-0 rounded-xl border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 shadow-sm divide-x divide-zinc-200 dark:divide-zinc-700">
      <Popover open={openCheckin} onOpenChange={setOpenCheckin}>
        <PopoverTrigger asChild>
          <button
            type="button"
            className="flex items-center gap-2.5 px-4 py-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/50 transition-colors flex-1 min-w-0 cursor-pointer">
            <CalendarIcon className="size-4 text-rose-500 shrink-0" />
            <div className="text-left min-w-0">
              <p className="text-[10px] uppercase tracking-[0.15em] text-zinc-400 font-medium leading-none mb-1">
                Check-in
              </p>
              <p className="text-sm font-medium text-zinc-900 dark:text-white truncate">
                {formatDateDayMonth(checkIn)}
              </p>
            </div>
          </button>
        </PopoverTrigger>
        <PopoverContent
          side="bottom"
          align="start"
          sideOffset={8}
          className="w-auto max-w-fit rounded-2xl p-0 overflow-hidden border shadow-xl"
          style={{ width: "auto", maxWidth: "none" }}>
          <div className="p-4">
            <Calendar
              className="w-auto"
              mode="single"
              selected={checkIn}
              onSelect={handleSelectCheckin}
              numberOfMonths={1}
              disabled={disabledDays}
              locale={es}
            />
          </div>
        </PopoverContent>
      </Popover>

      <Popover open={openCheckout} onOpenChange={setOpenCheckout}>
        <PopoverTrigger asChild>
          <button
            type="button"
            className="flex items-center gap-2.5 px-4 py-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/50 transition-colors flex-1 min-w-0 cursor-pointer">
            <CalendarIcon className="size-4 text-rose-500 shrink-0" />
            <div className="text-left min-w-0">
              <p className="text-[10px] uppercase tracking-[0.15em] text-zinc-400 font-medium leading-none mb-1">
                Check-out
              </p>
              <p className="text-sm font-medium text-zinc-900 dark:text-white truncate">
                {formatDateDayMonth(checkOut)}
              </p>
            </div>
          </button>
        </PopoverTrigger>
        <PopoverContent
          side="bottom"
          align="start"
          sideOffset={8}
          className="w-auto max-w-fit rounded-2xl p-0 overflow-hidden border shadow-xl"
          style={{ width: "auto", maxWidth: "none" }}>
          <div className="p-4">
            <Calendar
              className="w-auto"
              mode="single"
              selected={checkOut}
              onSelect={handleSelectCheckout}
              numberOfMonths={1}
              disabled={disabledCheckout}
              locale={es}
            />
          </div>
        </PopoverContent>
      </Popover>

      {noches > 0 && (
        <div className="flex items-center gap-1.5 px-4 py-3 shrink-0">
          <MoonIcon className="size-3.5 text-rose-500" />
          <span className="text-xs font-semibold text-rose-600 dark:text-rose-400 whitespace-nowrap">
            {noches} {noches === 1 ? "noche" : "noches"}
          </span>
        </div>
      )}
    </div>
  );
};

export default FechasBar;
