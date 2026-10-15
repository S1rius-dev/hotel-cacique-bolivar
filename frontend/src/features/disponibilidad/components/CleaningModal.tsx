import { motion } from "motion/react";
import { Sparkles, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { useMarcarComoLimpia } from "../hooks/useMarcarComoLimpia";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  habitacion: HabitacionEstadoDiario | null;
  onClose: () => void;
}

function CleaningModal({ open, habitacion, onClose }: Props) {
  const mutation = useMarcarComoLimpia();

  if (!open || !habitacion) return null;

  const handleConfirm = async () => {
    await mutation.mutateAsync({
      habitacionId: habitacion.id!,
    });
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-sky-600" />
            Limpieza: Habitación {habitacion.numero}
          </DialogTitle>
          <DialogDescription>
            Confirme que la limpieza de esta habitación ha sido completada.
          </DialogDescription>
        </DialogHeader>

        <motion.div
          initial="hidden"
          animate="visible"
          variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.08 } } }}
          className="space-y-3 py-2">
          <motion.div
            variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
            className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Habitación</span>
              <span className="font-medium">{habitacion.numero} · {habitacion.tipoNombre}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Estado actual</span>
              <span className="font-medium text-sky-600 dark:text-sky-400">En limpieza</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Nuevo estado</span>
              <span className="font-medium text-emerald-600 dark:text-emerald-400">Disponible</span>
            </div>
          </motion.div>
        </motion.div>

        <DialogFooter>
          <Button variant="ghost" onClick={onClose}>Cancelar</Button>
          <Button onClick={handleConfirm} disabled={mutation.isPending} className="bg-sky-600 hover:bg-sky-700">
            {mutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            Confirmar limpieza
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default CleaningModal;
