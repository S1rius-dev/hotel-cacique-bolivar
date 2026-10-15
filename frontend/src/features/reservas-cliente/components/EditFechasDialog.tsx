import { useState } from "react";
import { CalendarDays, CalendarIcon, Loader2, X } from "lucide-react";
import { format } from "date-fns";
import { es } from "date-fns/locale";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";
import { Popover, PopoverTrigger } from "@/components/ui/popover";
import { PublicPopoverContent } from "@/components/ui/public-popover";
import { Calendar } from "@/components/ui/calendar";
import { formatDate } from "@/utils/date";

interface Props {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  onConfirm: (checkIn: string, checkOut: string) => void;
  isPending?: boolean;
  currentCheckIn?: string;
  currentCheckOut?: string;
}

function toDateInputValue(dateStr?: string): string {
  if (!dateStr) return "";
  return dateStr.split("T")[0];
}

export default function EditFechasDialog({
  open,
  onOpenChange,
  onConfirm,
  isPending,
  currentCheckIn,
  currentCheckOut,
}: Props) {
  const [checkIn, setCheckIn] = useState(toDateInputValue(currentCheckIn));
  const [checkOut, setCheckOut] = useState(toDateInputValue(currentCheckOut));
  const [showCheckInConfirm, setShowCheckInConfirm] = useState(false);
  const [checkInOpen, setCheckInOpen] = useState(false);
  const [checkOutOpen, setCheckOutOpen] = useState(false);

  const checkInDate = checkIn ? new Date(checkIn + "T00:00:00") : undefined;
  const checkOutDate = checkOut ? new Date(checkOut + "T00:00:00") : undefined;
  const checkInChanged = checkIn !== toDateInputValue(currentCheckIn);
  const isValid = checkIn && checkOut && checkOut > checkIn;

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const disabledPast = (d: Date) => {
    const cmp = new Date(d);
    cmp.setHours(0, 0, 0, 0);
    return cmp < today;
  };

  const disabledCheckout = (d: Date) => {
    if (disabledPast(d)) return true;
    if (checkInDate) {
      const cmp = new Date(d);
      cmp.setHours(0, 0, 0, 0);
      return cmp <= checkInDate;
    }
    return false;
  };

  const handleConfirm = () => {
    if (!isValid) return;
    if (checkInChanged) {
      setShowCheckInConfirm(true);
    } else {
      onConfirm(checkIn, checkOut);
    }
  };

  const confirmAfterCheckInChange = () => {
    setShowCheckInConfirm(false);
    onConfirm(checkIn, checkOut);
  };

  const cancelCheckInChange = () => {
    setShowCheckInConfirm(false);
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <PublicDialogContent className="w-[95vw] sm:max-w-sm rounded-2xl p-6">
          <button
            type="button"
            onClick={() => onOpenChange(false)}
            className="absolute right-4 top-4 cursor-pointer text-stone-400 hover:text-stone-600">
            <X className="size-4" />
          </button>

          <div className="flex flex-col items-center gap-4">
            <div className="flex size-12 items-center justify-center rounded-full bg-rose-50">
              <CalendarDays className="size-6 text-rose-500" />
            </div>

            <DialogHeader>
              <DialogTitle>Editar fechas</DialogTitle>
              <DialogDescription>
                Selecciona las nuevas fechas para tu estadía
              </DialogDescription>
            </DialogHeader>

            <div className="w-full space-y-4">
              <div>
                <label className="mb-1.5 block text-xs font-medium text-stone-500 uppercase tracking-wider">
                  Nuevo check-in
                </label>
                <Popover open={checkInOpen} onOpenChange={setCheckInOpen}>
                  <PopoverTrigger asChild>
                    <button
                      type="button"
                      className="w-full flex items-center gap-2 rounded-xl border border-stone-200 px-4 py-2.5 text-sm text-stone-700 transition hover:border-rose-300 focus:border-rose-300 focus:ring-2 focus:ring-rose-100 cursor-pointer">
                      <CalendarIcon className="size-4 text-rose-500 shrink-0" />
                      <span className="flex-1 text-left">
                        {checkInDate
                          ? format(checkInDate, "dd MMM yyyy", { locale: es })
                          : "Seleccionar check-in"}
                      </span>
                    </button>
                  </PopoverTrigger>
                  <PublicPopoverContent
                    className="w-auto rounded-2xl p-0 overflow-hidden border shadow-xl force-light bg-white"
                    align="center"
                    style={{ width: "auto", maxWidth: "none" }}
                  >
                    <div className="p-4 force-light bg-white rounded-2xl">
                      <Calendar
                        mode="single"
                        selected={checkInDate}
                        onSelect={(d) => {
                          if (d) {
                            const newVal = format(d, "yyyy-MM-dd");
                            setCheckIn(newVal);
                            if (checkOutDate && d >= checkOutDate) {
                              setCheckOut("");
                            }
                          }
                          setCheckInOpen(false);
                        }}
                        disabled={disabledPast}
                        locale={es}
                        className="force-light bg-white"
                      />
                    </div>
                  </PublicPopoverContent>
                </Popover>
                {currentCheckIn && (
                  <p className="mt-1 text-xs text-stone-400">
                    check-In actual: {formatDate(currentCheckIn)}
                  </p>
                )}
              </div>

              <div>
                <label className="mb-1.5 block text-xs font-medium text-stone-500 uppercase tracking-wider">
                  Nuevo check-out
                </label>
                <Popover open={checkOutOpen} onOpenChange={setCheckOutOpen}>
                  <PopoverTrigger asChild>
                    <button
                      type="button"
                      className="w-full flex items-center gap-2 rounded-xl border border-stone-200 px-4 py-2.5 text-sm text-stone-700 transition hover:border-rose-300 focus:border-rose-300 focus:ring-2 focus:ring-rose-100 cursor-pointer">
                      <CalendarIcon className="size-4 text-rose-500 shrink-0" />
                      <span className="flex-1 text-left">
                        {checkOutDate
                          ? format(checkOutDate, "dd MMM yyyy", { locale: es })
                          : "Seleccionar check-out"}
                      </span>
                    </button>
                  </PopoverTrigger>
                  <PublicPopoverContent
                    className="w-auto rounded-2xl p-0 overflow-hidden border shadow-xl force-light bg-white"
                    align="center"
                    style={{ width: "auto", maxWidth: "none" }}
                  >
                    <div className="p-4 force-light bg-white rounded-2xl">
                      <Calendar
                        mode="single"
                        selected={checkOutDate}
                        onSelect={(d) => {
                          if (d) {
                            setCheckOut(format(d, "yyyy-MM-dd"));
                          }
                          setCheckOutOpen(false);
                        }}
                        disabled={disabledCheckout}
                        locale={es}
                        className="force-light bg-white"
                      />
                    </div>
                  </PublicPopoverContent>
                </Popover>
                {currentCheckOut && (
                  <p className="mt-1 text-xs text-stone-400">
                    check-Out actual: {formatDate(currentCheckOut)}
                  </p>
                )}
              </div>
            </div>

            <div className="flex w-full gap-3 pt-2">
              <button
                type="button"
                onClick={() => onOpenChange(false)}
                className="flex-1 rounded-xl border border-stone-200 py-2.5 text-sm font-medium text-stone-600 transition hover:bg-stone-50 cursor-pointer">
                Volver
              </button>
              <button
                type="button"
                onClick={handleConfirm}
                disabled={!isValid || isPending}
                className="flex-1 inline-flex items-center justify-center gap-2 rounded-xl bg-rose-800 py-2.5 text-sm font-medium text-white transition hover:bg-rose-900 disabled:opacity-50 cursor-pointer">
                {isPending && <Loader2 className="size-4 animate-spin" />}
                {isPending ? "Guardando..." : "Cambiar"}
              </button>
            </div>
          </div>
        </PublicDialogContent>
      </Dialog>

      <Dialog open={showCheckInConfirm} onOpenChange={cancelCheckInChange}>
        <PublicDialogContent className="w-[95vw] sm:max-w-sm rounded-2xl p-6">
          <button
            type="button"
            onClick={cancelCheckInChange}
            className="absolute right-4 top-4 cursor-pointer text-stone-400 hover:text-stone-600">
            <X className="size-4" />
          </button>

          <div className="flex flex-col items-center gap-4 text-center">
            <div className="flex size-12 items-center justify-center rounded-full bg-amber-50">
              <CalendarDays className="size-6 text-amber-600" />
            </div>

            <DialogHeader>
              <DialogTitle>Cambiar fecha de check-in</DialogTitle>
              <DialogDescription>
                Al modificar la fecha de check-in, la duración de la estadía se
                recalculará automáticamente.
              </DialogDescription>
            </DialogHeader>

            {checkInChanged && (
              <p className="text-sm text-stone-600">
                Nueva fecha: <strong>{formatDate(checkIn)}</strong>
              </p>
            )}

            <div className="flex w-full gap-3 pt-2">
              <button
                type="button"
                onClick={cancelCheckInChange}
                className="flex-1 rounded-xl border border-stone-200 py-2.5 text-sm font-medium text-stone-600 transition hover:bg-stone-50 cursor-pointer">
                Volver
              </button>
              <button
                type="button"
                onClick={confirmAfterCheckInChange}
                disabled={isPending}
                className="flex-1 inline-flex items-center justify-center gap-2 rounded-xl bg-rose-800 py-2.5 text-sm font-medium text-white transition hover:bg-rose-900 disabled:opacity-50 cursor-pointer">
                {isPending && <Loader2 className="size-4 animate-spin" />}
                {isPending ? "Guardando..." : "Sí, confirmar"}
              </button>
            </div>
          </div>
        </PublicDialogContent>
      </Dialog>
    </>
  );
}
