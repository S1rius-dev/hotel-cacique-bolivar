import { Info } from "lucide-react";

export function RecordsInfoFooter() {
  return (
    <div className="rounded-xl border border-sky-200 bg-sky-50/50 p-4 dark:border-sky-900/50 dark:bg-sky-950/20">
      <div className="flex items-start gap-3">
        <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-sky-200/80 text-sky-800 dark:bg-sky-900/50 dark:text-sky-400">
          <Info className="size-3.5" />
        </div>
        <p className="text-sm text-sky-700 dark:text-sky-400/80">
          Los registros de huéspedes corresponden al historial de estancias
          completadas y permanecerán disponibles para futuras consultas y
          reportes.
        </p>
      </div>
    </div>
  );
}
