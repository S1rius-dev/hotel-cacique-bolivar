import { useState, useMemo, type MouseEvent } from "react";
import { CalendarIcon, ArrowRight, Moon } from "lucide-react";
import { format, differenceInDays, addDays } from "date-fns";
import { es } from "date-fns/locale";
import type { DateRange } from "react-day-picker";
import { useNavigate } from "react-router-dom";

import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { cn } from "@/lib/utils";
import { formatDateDayMonth } from "@/utils/date";

export default function BookingSearch() {
  const navigate = useNavigate();
  const [openCheckin, setOpenCheckin] = useState(false);
  const [openCheckout, setOpenCheckout] = useState(false);
  const [range, setRange] = useState<DateRange | undefined>(() => {
    const tomorrow = addDays(new Date(), 1);
    return { from: tomorrow, to: addDays(tomorrow, 1) };
  });

  const nights = useMemo(() => {
    if (!range?.from || !range?.to) return 0;
    return Math.max(0, differenceInDays(range.to, range.from));
  }, [range]);

  const disabledDays = (d: Date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return d < today;
  };

  const disabledCheckout = useMemo(() => {
    return (d: Date) => {
      if (disabledDays(d)) return true;
      if (range?.from) return d <= range.from;
      return false;
    };
  }, [range?.from]);

  const handleSelectCheckin = (r: DateRange | undefined) => {
    if (!r?.from) return;
    setRange((prev) => {
      const to = (prev?.to && prev.to > r.from!) ? prev.to : addDays(r.from!, 1);
      return { from: r.from!, to };
    });
    setOpenCheckin(false);
    setOpenCheckout(true);
  };

  const handleSelectCheckout = (r: DateRange | undefined) => {
    if (!r?.to) return;
    setRange((prev) => ({ from: prev?.from, to: r.to! }));
    setOpenCheckout(false);
  };

  const handleSubmit = (e: MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!range?.from || !range?.to) {
      if (!range?.from) setOpenCheckin(true);
      else setOpenCheckout(true);
      return;
    }
    const params = new URLSearchParams({
      checkIn: format(range.from, "yyyy-MM-dd"),
      checkOut: format(range.to, "yyyy-MM-dd"),
    });
    navigate(`/reservar?${params.toString()}`);
  };

  return (
    <div
      role="group"
      aria-label="Buscar disponibilidad"
      className={cn(
        "group relative inline-flex items-center",
        "gap-1 sm:gap-1.5 md:gap-2",
        "rounded-full p-2 sm:p-2.5 md:p-3",
        "bg-white/15 backdrop-blur-xl backdrop-saturate-150",
        "border border-white/25 ring-1 ring-white/10",
        "shadow-[0_20px_50px_-15px_rgba(0,0,0,0.5),0_0_0_1px_rgba(255,255,255,0.05)]",
        "hover:bg-white/20 hover:border-white/35",
        "transition-all duration-300",
        "max-w-full",
      )}>
      <Popover open={openCheckin} onOpenChange={setOpenCheckin}>
        <PopoverTrigger asChild>
          <div className="flex items-center gap-2.5 md:gap-3 px-4 sm:px-5 md:px-6 py-2 sm:py-2.5 md:py-3 rounded-full transition-colors duration-200 hover:bg-white/10 cursor-pointer">
            <CalendarIcon className="size-4 sm:size-5 text-rose-300 shrink-0" />
            <div className="text-left">
              <p className="hidden sm:block text-[10px] sm:text-[11px] uppercase tracking-[0.2em] text-white/65 font-medium leading-none mb-1.5">
                Check-in
              </p>
              <p className="text-sm sm:text-base text-white font-medium leading-none whitespace-nowrap">
                {formatDateDayMonth(range?.from)}
              </p>
            </div>
          </div>
        </PopoverTrigger>
        <PopoverContent
          side="top"
          align="start"
          sideOffset={16}
          style={{ width: "auto" }}
          className="w-auto max-w-fit rounded-2xl p-0 overflow-visible border bg-background shadow-2xl isolate">
          <div className="p-3">
            <Calendar
              className="w-auto"
              mode="range"
              selected={range}
              onSelect={handleSelectCheckin}
              numberOfMonths={1}
              disabled={disabledDays}
              locale={es}
            />
          </div>
        </PopoverContent>
      </Popover>

      <div className="h-8 sm:h-10 md:h-12 w-px bg-white/15" />

      <Popover open={openCheckout} onOpenChange={setOpenCheckout}>
        <PopoverTrigger asChild>
          <div className="flex items-center gap-2.5 md:gap-3 px-4 sm:px-5 md:px-6 py-2 sm:py-2.5 md:py-3 rounded-full transition-colors duration-200 hover:bg-white/10 cursor-pointer">
            <CalendarIcon className="size-4 sm:size-5 text-rose-300 shrink-0" />
            <div className="text-left">
              <p className="hidden sm:block text-[10px] sm:text-[11px] uppercase tracking-[0.2em] text-white/65 font-medium leading-none mb-1.5">
                Check-out
              </p>
              <p className="text-sm sm:text-base text-white font-medium leading-none whitespace-nowrap">
                {formatDateDayMonth(range?.to)}
              </p>
            </div>
          </div>
        </PopoverTrigger>
        <PopoverContent
          side="top"
          align="end"
          sideOffset={16}
          style={{ width: "auto" }}
          className="w-auto max-w-fit rounded-2xl p-0 overflow-visible border bg-background shadow-2xl isolate">
          <div className="p-3">
            <Calendar
              className="w-auto"
              mode="range"
              selected={range}
              onSelect={handleSelectCheckout}
              numberOfMonths={1}
              disabled={disabledCheckout}
              locale={es}
            />
          </div>
        </PopoverContent>
      </Popover>

      {nights > 0 && (
        <>
          <div className="hidden md:block h-8 w-px bg-white/15" />
          <div className="hidden md:flex items-center gap-1.5 px-4 py-2 rounded-full bg-white/10 border border-white/15">
            <Moon className="size-3.5 text-rose-300" />
            <span className="text-[11px] uppercase tracking-[0.15em] text-white font-medium whitespace-nowrap">
              {nights} {nights === 1 ? "noche" : "noches"}
            </span>
          </div>
        </>
      )}

      <button
        type="button"
        onClick={handleSubmit}
        className={cn(
          "ml-1.5 sm:ml-2 md:ml-3 flex items-center gap-2",
          "px-5 sm:px-7 md:px-8 py-3 sm:py-3.5 md:py-4 rounded-full",
          "bg-linear-to-br from-rose-500 via-rose-500 to-rose-600 text-white",
          "shadow-[0_8px_20px_-5px_rgba(244,63,94,0.5),inset_0_1px_0_rgba(255,255,255,0.25)]",
          "hover:from-rose-400 hover:via-rose-500 hover:to-rose-500",
          "hover:shadow-[0_12px_28px_-5px_rgba(244,63,94,0.65)]",
          "hover:-translate-y-0.5 active:translate-y-0",
          "transition-all duration-300 cursor-pointer",
          "text-sm sm:text-base font-semibold tracking-[0.2em] uppercase",
        )}
        aria-label="Ir a reservar">
        GO
        <ArrowRight className="size-4 sm:size-5" />
      </button>
    </div>
  );
}
