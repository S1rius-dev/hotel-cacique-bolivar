import { AlertTriangle, Loader2, X } from "lucide-react";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";

interface Props {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  onConfirm: () => void;
  isPending?: boolean;
  codigoReserva?: string;
}

export default function CancelReservaDialog({
  open,
  onOpenChange,
  onConfirm,
  isPending,
  codigoReserva,
}: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <PublicDialogContent className="w-[95vw] sm:max-w-sm rounded-2xl p-6">
        <button
          type="button"
          onClick={() => onOpenChange(false)}
          className="absolute right-4 top-4 cursor-pointer text-stone-400 hover:text-stone-600"
        >
          <X className="size-4" />
        </button>

        <div className="flex flex-col items-center gap-4 text-center">
          <div className="flex size-12 items-center justify-center rounded-full bg-red-50">
            <AlertTriangle className="size-6 text-red-500" />
          </div>

          <DialogHeader>
            <DialogTitle>Cancelar reserva</DialogTitle>
            <DialogDescription>
              ¿Estás seguro de cancelar la reserva{" "}
              <strong className="text-stone-700">{codigoReserva ?? ""}</strong>?
            </DialogDescription>
          </DialogHeader>

          <p className="text-xs text-stone-400 leading-relaxed">
            Al cancelar, la reserva pasará a estado{" "}
            <strong>Cancelada</strong>. Si realizaste algún pago, este se
            conservará según nuestra política de reembolsos.
          </p>

          <div className="flex w-full gap-3 pt-2">
            <button
              type="button"
              onClick={() => onOpenChange(false)}
              className="flex-1 rounded-xl border border-stone-200 py-2.5 text-sm font-medium text-stone-600 transition hover:bg-stone-50 cursor-pointer"
            >
              Volver
            </button>
            <button
              type="button"
              onClick={onConfirm}
              disabled={isPending}
              className="flex-1 inline-flex items-center justify-center gap-2 rounded-xl bg-red-600 py-2.5 text-sm font-medium text-white transition hover:bg-red-700 disabled:opacity-50 cursor-pointer"
            >
              {isPending && <Loader2 className="size-4 animate-spin" />}
              {isPending ? "Cancelando..." : "Sí, cancelar"}
            </button>
          </div>
        </div>
      </PublicDialogContent>
    </Dialog>
  );
}
