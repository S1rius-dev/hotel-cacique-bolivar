import { AlertTriangle, Loader2, X, ArrowLeftRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useReembolsarPago } from "../hooks/usePagos";
import PagoMetodoIcon from "./PagoMetodoIcon";
import PagoStatusBadge from "./PagoStatusBadge";
import { FORMAT_CURRENCY, getMetodoPagoLabel } from "../constants/pago.constants";
import { toast } from "sonner";
import type { PagoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";
import { extractErrorMessage } from "@/lib/extract-error";

interface Props {
  open: boolean;
  pago: PagoResponse;
  onClose: () => void;
  onSuccess?: () => void;
}

function ReembolsarPagoModal({ open, pago, onClose, onSuccess }: Props) {
  const mutation = useReembolsarPago();

  if (!open) return null;

  const handleConfirm = async () => {
    await mutation.mutateAsync(pago.id!, {
      onSuccess: () => {
        toast.success("Pago reembolsado exitosamente");
        onSuccess?.();
        onClose();
      },
      onError: (error) => {
        toast.error(extractErrorMessage(error, "Error al reembolsar el pago"));
      },
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />

      <div className="relative w-full max-w-sm mx-4">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-red-100 dark:bg-red-900/30">
                <AlertTriangle className="w-5 h-5 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  Reembolsar pago
                </h2>
                <p className="text-xs text-muted-foreground">
                  #{pago.codigoPago ?? pago.id}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              disabled={mutation.isPending}
              className="p-1 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
            >
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          {/* Body */}
          <div className="p-5 space-y-4">
            <div className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-4 space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Monto</span>
                <span className="font-bold text-lg text-zinc-900 dark:text-zinc-100">
                  {FORMAT_CURRENCY.format(pago.monto ?? 0)}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Método</span>
                <span className="font-medium flex items-center gap-1.5">
                  <PagoMetodoIcon metodo={pago.metodoPago} className="w-5 h-5" />
                  {getMetodoPagoLabel(pago.metodoPago)}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Estado</span>
                <PagoStatusBadge estado={pago.estado} />
              </div>
              {pago.fechaPago && (
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Fecha</span>
                  <span className="font-medium">
                    {formatDate(pago.fechaPago)}
                  </span>
                </div>
              )}
            </div>

            <div className="rounded-lg border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-950/30 p-3 text-xs text-red-700 dark:text-red-400 space-y-1">
              <p className="font-medium flex items-center gap-1.5">
                <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                Esta acción no se puede deshacer
              </p>
              <p>
                Se revertirá el pago y el saldo pendiente de la reserva se actualizará.
              </p>
            </div>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50">
            <Button
              variant="ghost"
              onClick={onClose}
              disabled={mutation.isPending}
              className="text-muted-foreground hover:text-foreground"
            >
              Cancelar
            </Button>
            <Button
              onClick={handleConfirm}
              disabled={mutation.isPending}
              className="gap-2 bg-red-600 hover:bg-red-700 text-white"
            >
              {mutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
              <ArrowLeftRight className="w-4 h-4" />
              {mutation.isPending ? "Reembolsando..." : "Confirmar reembolso"}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ReembolsarPagoModal;
