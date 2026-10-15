import { AlertTriangle, X } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Props {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  description?: string;
  confirmLabel?: string;
  isPending?: boolean;
}

export default function ConfirmDeleteDialog({
  open,
  onClose,
  onConfirm,
  title = "¿Estás seguro de eliminar?",
  description = "Esta acción no se puede deshacer. El registro será eliminado permanentemente.",
  confirmLabel = "Eliminar",
  isPending = false,
}: Props) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className="relative w-full max-w-sm mx-4">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-red-100 dark:bg-red-900/30">
                <AlertTriangle className="w-5 h-5 text-red-600 dark:text-red-400" />
              </div>
              <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                {title}
              </h2>
            </div>
            <button
              onClick={onClose}
              className="p-1 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors">
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          <div className="p-5">
            <p className="text-sm text-muted-foreground leading-relaxed">
              {description}
            </p>
          </div>

          <div className="flex items-center justify-end gap-3 px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50">
            <Button
              variant="ghost"
              onClick={onClose}
              disabled={isPending}
              className="text-muted-foreground hover:text-foreground">
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={onConfirm}
              disabled={isPending}
              className="gap-2">
              {isPending ? (
                <span className="animate-spin">⟳</span>
              ) : (
                <AlertTriangle className="w-4 h-4" />
              )}
              {confirmLabel}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}