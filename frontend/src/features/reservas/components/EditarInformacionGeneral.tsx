import { format, addDays } from "date-fns";
import { es } from "date-fns/locale";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { ORIGEN_OPTIONS } from "../constants/reserva.constants";

interface Props {
  checkIn: string;
  checkOut: string;
  origen: string;
  observaciones: string;
  checkInOpen: boolean;
  checkOutOpen: boolean;
  onCheckInChange: (val: string) => void;
  onCheckOutChange: (val: string) => void;
  onOrigenChange: (val: string) => void;
  onObservacionesChange: (val: string) => void;
  onCheckInOpenChange: (val: boolean) => void;
  onCheckOutOpenChange: (val: boolean) => void;
}

function EditarInformacionGeneral({
  checkIn,
  checkOut,
  origen,
  observaciones,
  checkInOpen,
  checkOutOpen,
  onCheckInChange,
  onCheckOutChange,
  onOrigenChange,
  onObservacionesChange,
  onCheckInOpenChange,
  onCheckOutOpenChange,
}: Props) {
  const checkInDate = checkIn ? new Date(checkIn + "T00:00:00") : undefined;
  const checkOutDate = checkOut ? new Date(checkOut + "T00:00:00") : undefined;
  const nights =
    checkInDate && checkOutDate
      ? Math.round(
          (checkOutDate.getTime() - checkInDate.getTime()) /
            (1000 * 60 * 60 * 24),
        )
      : 0;

  return (
    <div className="rounded-2xl border border-border bg-muted/50 p-6 shadow-sm">
      <h3 className="font-heading text-base font-medium text-foreground mb-5">
        Fechas de estadía
      </h3>

      <div className="flex items-center gap-4 mb-6">
        <Popover open={checkInOpen} onOpenChange={onCheckInOpenChange}>
          <PopoverTrigger asChild>
            <button
              type="button"
              className="flex flex-col items-center gap-1 px-4 py-3 rounded-xl border border-border bg-card hover:border-rose-700/40 transition-colors cursor-pointer min-w-[100px]"
            >
              <span className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground font-medium">
                Check-in
              </span>
              <span className="text-lg font-semibold font-mono text-foreground tabular-nums leading-none">
                {checkInDate ? format(checkInDate, "dd MMM", { locale: es }) : "—"}
              </span>
              <span className="text-[10px] text-muted-foreground">
                {checkInDate ? format(checkInDate, "yyyy", { locale: es }) : ""}
              </span>
            </button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="center">
            <Calendar
              mode="single"
              selected={checkInDate}
              onSelect={(d) => {
                if (d) {
                  const newVal = format(d, "yyyy-MM-dd");
                  onCheckInChange(newVal);
                  if (checkOutDate && d >= checkOutDate) {
                    const nextDay = format(addDays(d, 1), "yyyy-MM-dd");
                    onCheckOutChange(nextDay);
                  }
                }
                onCheckInOpenChange(false);
              }}
              disabled={(d) => d < new Date(new Date().toDateString())}
              locale={es}
            />
          </PopoverContent>
        </Popover>

        <div className="flex-1 flex flex-col items-center gap-1">
          <div className="w-full flex items-center gap-1">
            <div className="h-[2px] flex-1 bg-gradient-to-r from-rose-700/20 to-rose-700/40" />
            <div className="size-1 rounded-full bg-rose-700/30" />
            <div className="h-[2px] flex-1 bg-gradient-to-r from-rose-700/40 to-rose-700/20" />
          </div>
          <span className="text-[11px] font-medium text-rose-700/70 tracking-wide uppercase">
            {nights} {nights === 1 ? "noche" : "noches"}
          </span>
        </div>

        <Popover open={checkOutOpen} onOpenChange={onCheckOutOpenChange}>
          <PopoverTrigger asChild>
            <button
              type="button"
              className="flex flex-col items-center gap-1 px-4 py-3 rounded-xl border border-border bg-card hover:border-rose-700/40 transition-colors cursor-pointer min-w-[100px]"
            >
              <span className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground font-medium">
                Check-out
              </span>
              <span className="text-lg font-semibold font-mono text-foreground tabular-nums leading-none">
                {checkOutDate ? format(checkOutDate, "dd MMM", { locale: es }) : "—"}
              </span>
              <span className="text-[10px] text-muted-foreground">
                {checkOutDate ? format(checkOutDate, "yyyy", { locale: es }) : ""}
              </span>
            </button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="center">
            <Calendar
              mode="single"
              selected={checkOutDate}
              onSelect={(d) => {
                if (d) {
                  onCheckOutChange(format(d, "yyyy-MM-dd"));
                }
                onCheckOutOpenChange(false);
              }}
              disabled={(d) => {
                if (d < new Date(new Date().toDateString())) return true;
                if (checkInDate) return d <= checkInDate;
                return false;
              }}
              locale={es}
            />
          </PopoverContent>
        </Popover>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-1.5">
          <label className="text-xs text-muted-foreground font-medium tracking-wide">
            Origen
          </label>
          <select
            value={origen}
            onChange={(e) => onOrigenChange(e.target.value)}
            className="w-full h-9 rounded-lg border border-border bg-card px-3 text-sm text-foreground outline-none focus:border-rose-700/40 focus:ring-[3px] focus:ring-rose-700/10"
          >
            {ORIGEN_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1.5">
          <label className="text-xs text-muted-foreground font-medium tracking-wide">
            Noches
          </label>
          <p className="h-9 flex items-center text-sm font-mono font-semibold text-foreground tabular-nums">
            {nights > 0 ? `${nights} ${nights === 1 ? "noche" : "noches"}` : "—"}
          </p>
        </div>
      </div>

      <div className="mt-4 pt-4 border-t border-border">
        <label className="text-xs text-muted-foreground font-medium tracking-wide block mb-2">
          Observaciones
        </label>
        <textarea
          value={observaciones}
          onChange={(e) => onObservacionesChange(e.target.value)}
          rows={2}
          className="w-full rounded-lg border border-border bg-card px-3 py-2 text-sm text-foreground outline-none focus:border-rose-700/40 focus:ring-[3px] focus:ring-rose-700/10 resize-none"
          placeholder="Notas adicionales sobre la reserva..."
        />
      </div>
    </div>
  );
}

export default EditarInformacionGeneral;
