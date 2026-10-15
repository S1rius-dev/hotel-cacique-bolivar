import { Globe } from "lucide-react";
import { Switch } from "@/components/ui/switch";

interface Props {
  webEnabled: boolean;
  onToggle: (enabled: boolean) => void;
}

export function OnlineReservationStatus({ webEnabled, onToggle }: Props) {
  return (
    <div className="flex h-full items-center gap-4 rounded-xl border border-zinc-200 bg-card p-5 shadow-sm dark:border-zinc-800">
      <div className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-rose-100 dark:bg-rose-900/30">
        <Globe className="size-5 text-rose-700 dark:text-rose-400" />
      </div>

      <div className="min-w-0 flex-1">
        <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
          Estado de las reservas web
        </h3>
        <div className="mt-1 flex items-center gap-2">
          <span
            className={`inline-flex h-2 w-2 shrink-0 rounded-full ${
              webEnabled ? "bg-emerald-500" : "bg-zinc-400"
            }`}
          />
          <p
            className={`text-sm font-medium ${
              webEnabled
                ? "text-emerald-600 dark:text-emerald-400"
                : "text-zinc-500 dark:text-zinc-400"
            }`}
          >
            {webEnabled ? "Habilitadas" : "Deshabilitadas"}
          </p>
        </div>
        <p className="mt-0.5 text-xs text-muted-foreground">
          {webEnabled
            ? "Los huéspedes pueden realizar reservas desde la página web."
            : "Las reservas desde la página web están deshabilitadas temporalmente."}
        </p>
      </div>

      <Switch
        checked={webEnabled}
        onCheckedChange={onToggle}
        aria-label="Habilitar reservas web"
        className="shrink-0 cursor-pointer"
      />
    </div>
  );
}