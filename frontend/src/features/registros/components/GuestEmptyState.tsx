import { SearchX } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Props {
  onRefresh?: () => void;
}

export function GuestEmptyState({ onRefresh }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="mb-5 flex size-16 items-center justify-center rounded-2xl bg-zinc-100 dark:bg-zinc-800">
        <SearchX className="size-8 text-zinc-400 dark:text-zinc-500" />
      </div>
      <h3 className="mb-1.5 text-lg font-semibold text-zinc-900 dark:text-zinc-100">
        No existen registros de huéspedes
      </h3>
      <p className="mb-6 max-w-md text-sm text-muted-foreground">
        Los huéspedes aparecerán aquí cuando existan reservas finalizadas
        dentro del período consultado.
      </p>
      {onRefresh && (
        <Button variant="outline" onClick={onRefresh} className="cursor-pointer">
          Actualizar
        </Button>
      )}
    </div>
  );
}
