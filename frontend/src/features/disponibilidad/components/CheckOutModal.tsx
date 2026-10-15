import { motion } from "motion/react";
import { useState } from "react";
import { LogOut, Loader2, AlertTriangle, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { useCheckOutPorDetalle } from "../hooks/useCheckInOut";
import { FORMAT_CURRENCY, RegistrarPagoModal } from "@/features/pagos";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  habitacion: HabitacionEstadoDiario | null;
  onClose: () => void;
}

function CheckOutModal({ open, habitacion, onClose }: Props) {
  const mutation = useCheckOutPorDetalle();
  const [pagoModalOpen, setPagoModalOpen] = useState(false);

  if (!open || !habitacion || !habitacion.reservaActiva) return null;

  const saldoPendiente = habitacion.reservaActiva.saldoPendiente ?? 0;
  const total = habitacion.reservaActiva.total ?? 0;

  const huespedesList = habitacion.reservaActiva?.huespedes?.map((h) => h.nombreCompleto).join(", ");

  const handleConfirm = async () => {
    if (!habitacion.reservaActiva) return;

    await mutation.mutateAsync({
      reservaId: habitacion.reservaActiva.reservaId!,
      detalleId: habitacion.reservaActiva.detalleId!,
    });

    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <LogOut className="w-5 h-5 text-blue-600" />
            Check-out: Habitación {habitacion.numero}
          </DialogTitle>

          <DialogDescription>
            Confirme la salida de los huéspedes de esta habitación.
          </DialogDescription>
        </DialogHeader>

        <motion.div
          initial="hidden"
          animate="visible"
          variants={{
            hidden: {},
            visible: {
              transition: { staggerChildren: 0.08 },
            },
          }}
          className="space-y-3 py-2">
          <motion.div
            variants={{
              hidden: { opacity: 0, y: 10 },
              visible: { opacity: 1, y: 0 },
            }}
            className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Habitación</span>
              <span className="font-medium">
                {habitacion.numero} · {habitacion.tipoNombre}
              </span>
            </div>

            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Reserva</span>
              <span className="font-medium">
                {habitacion.reservaActiva.codigoReserva}
              </span>
            </div>

            {huespedesList && (
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Huéspedes</span>
                <span className="font-medium">{huespedesList}</span>
              </div>
            )}

            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Check-in</span>
              <span className="font-medium">{habitacion.reservaActiva.checkIn}</span>
            </div>

            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Check-out</span>
              <span className="font-medium">{habitacion.reservaActiva.checkOut}</span>
            </div>
          </motion.div>

          {saldoPendiente > 0 && (
            <motion.div
              variants={{
                hidden: { opacity: 0, x: -10 },
                visible: { opacity: 1, x: 0 },
              }}>
              <div className="rounded-lg border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-950/20 p-4 space-y-3">
                <div className="flex items-start gap-2">
                  <AlertTriangle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />

                  <div className="flex-1">
                    <p className="text-sm font-medium text-red-700 dark:text-red-400">
                      Saldo pendiente
                    </p>

                    <p className="text-xs text-red-600/70 dark:text-red-400/70 mt-0.5">
                      Esta reserva tiene un saldo pendiente de{" "}
                      <strong>{FORMAT_CURRENCY.format(saldoPendiente)}</strong>{" "}
                      de {FORMAT_CURRENCY.format(total)} total.
                      {saldoPendiente < total
                        ? " Se recomienda cobrar el saldo antes del check-out."
                        : " Se recomienda cobrar el total antes del check-out."}
                    </p>
                  </div>
                </div>

                <Button
                  variant="default"
                  size="sm"
                  onClick={() => setPagoModalOpen(true)}
                  className="gap-1.5 bg-emerald-700 hover:bg-emerald-800 text-xs w-full">
                  <Plus className="w-3.5 h-3.5" />
                  Cobrar {FORMAT_CURRENCY.format(saldoPendiente)} ahora
                </Button>
              </div>
            </motion.div>
          )}
        </motion.div>

        <DialogFooter>
          <Button variant="ghost" onClick={onClose}>
            Cancelar
          </Button>

          <Button
            onClick={handleConfirm}
            disabled={mutation.isPending}
            className="bg-blue-600 hover:bg-blue-700">
            {mutation.isPending && (
              <Loader2 className="w-4 h-4 mr-2 animate-spin" />
            )}
            Confirmar check-out
          </Button>
        </DialogFooter>
      </DialogContent>

      <RegistrarPagoModal
        open={pagoModalOpen}
        reservaId={habitacion.reservaActiva.reservaId!}
        total={total}
        totalPagado={habitacion.reservaActiva.totalPagado ?? 0}
        saldoPendiente={saldoPendiente}
        onClose={() => setPagoModalOpen(false)}
      />
    </Dialog>
  );
}

export default CheckOutModal;
