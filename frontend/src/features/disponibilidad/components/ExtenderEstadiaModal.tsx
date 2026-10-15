import { motion } from "motion/react";
import { useState } from "react";
import { addDays, format, parseISO } from "date-fns";
import { CalendarDays, Loader2, CalendarPlus, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { es } from "date-fns/locale";
import { useExtenderEstadia } from "../hooks/useCheckInOut";
import { formatCurrency } from "@/utils/format";
import type { ReservaActiva } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  reserva: ReservaActiva | null;
  onClose: () => void;
}

function ExtenderEstadiaModal({ open, reserva, onClose }: Props) {
  const mutation = useExtenderEstadia();
  const [nuevaFecha, setNuevaFecha] = useState<Date | undefined>(undefined);

  if (!open || !reserva) return null;

  const currentCheckOut = parseISO(reserva.checkOut ?? "");
  const minDate = addDays(currentCheckOut, 1);
  const selectedDate = nuevaFecha ?? minDate;

  const handleConfirm = async () => {
    await mutation.mutateAsync({
      reservaId: reserva.reservaId!,
      nuevaFechaCheckOut: format(selectedDate, "yyyy-MM-dd"),
    });
    handleClose();
  };

  const handleClose = () => {
    setNuevaFecha(undefined);
    onClose();
  };

  const nochesExtras = Math.ceil(
    (selectedDate.getTime() - currentCheckOut.getTime()) /
      (1000 * 60 * 60 * 24),
  );

  const precioTotal =
    reserva.precioPorNoche != null
      ? reserva.precioPorNoche * nochesExtras
      : null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={handleClose}
      />

      <div className="relative w-full max-w-sm mx-4">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/30">
                <CalendarPlus className="w-5 h-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  Extender estadía
                </h2>
                <p className="text-xs text-muted-foreground">
                  Reserva {reserva.codigoReserva}
                </p>
              </div>
            </div>
            <button
              onClick={handleClose}
              className="p-1 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors">
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          {/* Body */}
          <motion.div
            initial="hidden"
            animate="visible"
            variants={{
              hidden: {},
              visible: { transition: { staggerChildren: 0.08 } },
            }}
            className="p-5 space-y-4">
            <motion.div
              variants={{
                hidden: { opacity: 0, y: 10 },
                visible: { opacity: 1, y: 0 },
              }}
              className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-4 space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Check-out actual</span>
                <span className="font-medium">{reserva.checkOut}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Nuevo check-out</span>
                <span className="font-medium text-blue-600 dark:text-blue-400">
                  {format(selectedDate, "dd/MM/yyyy", { locale: es })}
                </span>
              </div>
              <div className="flex justify-between text-sm pt-2 border-t border-zinc-200 dark:border-zinc-700">
                <span className="text-muted-foreground">Noches extras</span>
                <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                  +{nochesExtras} {nochesExtras === 1 ? "noche" : "noches"}
                </span>
              </div>

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                  Precio por noche
                </span>
                <span className="font-medium">
                  {reserva.precioPorNoche != null
                    ? formatCurrency(reserva.precioPorNoche)
                    : "-"}
                </span>
              </div>
              <div className="flex justify-between text-base font-semibold pt-2 border-t border-zinc-200 dark:border-zinc-700">
                <span className="text-foreground">Total a pagar</span>
                <span className="text-blue-600 dark:text-blue-400">
                  {precioTotal != null
                    ? formatCurrency(precioTotal)
                    : "-"}
                </span>
              </div>
            </motion.div>

            <motion.div
              variants={{
                hidden: { opacity: 0, y: 10 },
                visible: { opacity: 1, y: 0 },
              }}
              className="space-y-1.5">
              <label className="text-xs font-medium text-muted-foreground">
                Seleccionar nueva fecha
              </label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className="w-full justify-start text-left font-normal">
                    <CalendarDays className="mr-2 h-4 w-4" />
                    {format(selectedDate, "dd/MM/yyyy", { locale: es })}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={selectedDate}
                    onSelect={(d) => d && setNuevaFecha(d)}
                    disabled={{ before: minDate }}
                    locale={es}
                  />
                </PopoverContent>
              </Popover>
            </motion.div>
          </motion.div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50">
            <Button
              variant="ghost"
              onClick={handleClose}
              className="text-muted-foreground hover:text-foreground">
              Cancelar
            </Button>
            <Button
              onClick={handleConfirm}
              disabled={mutation.isPending}
              className="gap-2 bg-blue-600 hover:bg-blue-700 text-white">
              {mutation.isPending && (
                <Loader2 className="w-4 h-4 animate-spin" />
              )}
              {precioTotal != null
                ? `${formatCurrency(precioTotal)} · ${nochesExtras} ${nochesExtras === 1 ? "noche" : "noches"}`
                : `Extender (${nochesExtras} ${nochesExtras === 1 ? "noche" : "noches"})`}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ExtenderEstadiaModal;
