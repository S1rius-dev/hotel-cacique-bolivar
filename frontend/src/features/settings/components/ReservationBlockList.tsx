import { CalendarX2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import type { BookingBlock } from "../types";
import { ReservationBlockCard } from "./ReservationBlockCard";

interface Props {
  blocks: BookingBlock[];
  loading?: boolean;
  onEdit: (block: BookingBlock) => void;
  onDeactivate: (block: BookingBlock) => void;
  onReactivate: (block: BookingBlock) => void;
  onCreate: () => void;
}

export function ReservationBlockList({
  blocks,
  loading,
  onEdit,
  onDeactivate,
  onReactivate,
  onCreate,
}: Props) {
  if (loading) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
      </div>
    );
  }

  if (blocks.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-300 py-8 text-center dark:border-zinc-700">
        <div className="mb-3 flex size-10 items-center justify-center rounded-lg bg-zinc-100 dark:bg-zinc-800">
          <CalendarX2 className="size-5 text-zinc-400 dark:text-zinc-500" />
        </div>
        <p className="text-sm font-medium text-foreground">
          No existen bloqueos programados
        </p>
        <p className="mt-1 max-w-sm text-xs text-muted-foreground">
          Puedes crear un bloqueo para impedir reservas web durante
          mantenimientos, eventos privados o cierres temporales.
        </p>
        <Button
          variant="hotel"
          size="sm"
          onClick={onCreate}
          className="mt-4 cursor-pointer"
        >
          <CalendarX2 className="size-4" />
          Crear bloqueo
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {blocks.map((block) => (
        <ReservationBlockCard
          key={block.id}
          block={block}
          onEdit={onEdit}
          onDeactivate={onDeactivate}
          onReactivate={onReactivate}
        />
      ))}
    </div>
  );
}