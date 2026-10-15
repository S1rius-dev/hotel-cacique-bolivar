import { Info } from "lucide-react";

export function BlockingInfoAlert() {
  return (
    <div className="rounded-xl border border-amber-200 bg-amber-50/50 p-4 dark:border-amber-900/50 dark:bg-amber-950/20">
      <div className="flex items-start gap-3">
        <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-amber-200/80 text-amber-800 dark:bg-amber-900/50 dark:text-amber-400">
          <Info className="size-3.5" />
        </div>
        <div className="space-y-1 text-sm">
          <p className="font-medium text-amber-900 dark:text-amber-300">
            Información importante
          </p>
          <p className="text-amber-700 dark:text-amber-400/80">
            Los bloqueos únicamente afectan las reservas realizadas desde la
            página web. Las reservas creadas desde Recepción, Teléfono u otros
            canales internos <strong>no serán afectadas</strong>.
          </p>
        </div>
      </div>
    </div>
  );
}
