import { useState, useCallback } from "react";
import { addDays } from "date-fns";
import { es } from "date-fns/locale";
import { CalendarDays } from "lucide-react";

import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";

interface Props {
  checkIn: string;
  checkOut: string;
  nights: number;
  checkInDate: Date;
  checkOutDate: Date;
  onCheckInChange: (date: Date) => void;
  onCheckOutChange: (date: Date) => void;
}

function DatePicker({
  label,
  value,
  date,
  align,
  minDate,
  maxDate,
  onSelect,
}: {
  label: string;
  value: string;
  date: Date;
  align: "start" | "end";
  minDate?: Date;
  maxDate?: Date;
  onSelect: (d: Date) => void;
}) {
  const [open, setOpen] = useState(false);

  const disabledDays = (d: Date) => {
    const day = new Date(d);
    day.setHours(0, 0, 0, 0);
    if (minDate) {
      const min = new Date(minDate);
      min.setHours(0, 0, 0, 0);
      if (day < min) return true;
    }
    if (maxDate) {
      const max = new Date(maxDate);
      max.setHours(0, 0, 0, 0);
      if (day > max) return true;
    }
    return false;
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type="button"
          className="flex flex-col items-start gap-0.5 cursor-pointer transition-colors hover:bg-rose-50 rounded-lg px-3 py-2">
          <span className="text-[10px] uppercase tracking-wider text-stone-400 font-medium">
            {label}
          </span>
          <span className="text-sm font-semibold text-stone-800 whitespace-nowrap">
            {value}
          </span>
        </button>
      </PopoverTrigger>
      <PopoverContent
        side="bottom"
        align={align}
        sideOffset={12}
        className="w-auto max-w-fit rounded-2xl p-0 overflow-visible border bg-background shadow-2xl isolate"
        style={{ width: "auto" }}>
        <div className="p-3">
          <Calendar
            className="w-auto"
            mode="single"
            selected={date}
            onSelect={(d) => {
              if (d) {
                onSelect(d);
                setOpen(false);
              }
            }}
            disabled={disabledDays}
            locale={es}
          />
        </div>
      </PopoverContent>
    </Popover>
  );
}

export default function BookingHeroSection({
  checkIn,
  checkOut,
  nights,
  checkInDate,
  checkOutDate,
  onCheckInChange,
  onCheckOutChange,
}: Props) {
  const handleCheckInChange = useCallback(
    (date: Date) => {
      onCheckInChange(date);
      if (date >= checkOutDate) {
        onCheckOutChange(addDays(date, 1));
      }
    },
    [checkOutDate, onCheckInChange, onCheckOutChange],
  );

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const checkOutMin = new Date(checkInDate.getTime() + 86400000);
  checkOutMin.setHours(0, 0, 0, 0);

  return (
    <div className="absolute bottom-0 left-0 right-0 z-10 pb-24 sm:pb-10 md:pb-16">
      <div className="mx-auto max-w-7xl px-4 md:px-8 w-full">
        <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-6 lg:gap-8">
          <div className="max-w-xl">
            <div className="hidden sm:flex items-center gap-3 opacity-90 mb-3">
              <span className="h-px w-8 bg-rose-300/70" />
              <p className="font-sans text-[10px] sm:text-xs tracking-[0.35em] uppercase text-white/85 font-medium">
                Hotel Cacique de Bolivar
              </p>
            </div>
            <h1 className="font-heading text-4xl sm:text-5xl lg:text-6xl text-white leading-[1.08] font-medium tracking-tight">
              Reserva tu
              <br />
              <span className="italic font-light text-rose-300">
                estancia en Amalfi
              </span>
            </h1>
            <p className="mt-4 text-sm text-white/70 font-sans leading-relaxed max-w-md">
              Selecciona las fechas de tu viaje y elige las habitaciones que
              mejor se adapten a tus necesidades.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3 bg-white/95 backdrop-blur-sm rounded-2xl px-5 py-4 border border-white/30 shadow-2xl shrink-0">
            <div className="flex items-center gap-1">
              <DatePicker
                label="Check-in"
                value={checkIn}
                date={checkInDate}
                align="start"
                minDate={today}
                onSelect={handleCheckInChange}
              />
              <span className="text-rose-300 text-lg mx-1">→</span>
              <DatePicker
                label="Check-out"
                value={checkOut}
                date={checkOutDate}
                align="end"
                minDate={checkOutMin}
                onSelect={onCheckOutChange}
              />
            </div>

            <div
              aria-hidden
              className="hidden sm:block w-px h-8 bg-stone-200"
            />

            <div className="flex items-center gap-2 px-2">
              <CalendarDays className="size-4 text-rose-400" />
              <span className="text-lg font-bold text-rose-600">{nights}</span>
              <span className="text-xs text-stone-400">
                {nights === 1 ? "noche" : "noches"}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
