import { Lock, Pencil, Power, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { formatDateShort } from "@/utils/date";
import {
  canReactivateBlock,
  deriveReservationBlockState,
  type BookingBlock,
} from "../types";
import { BLOCK_STATE_CONFIG } from "./constants";

interface Props {
  block: BookingBlock;
  onEdit: (block: BookingBlock) => void;
  onDeactivate: (block: BookingBlock) => void;
  onReactivate: (block: BookingBlock) => void;
}

export function ReservationBlockCard({
  block,
  onEdit,
  onDeactivate,
  onReactivate,
}: Props) {
  const state = deriveReservationBlockState(block);

  return (
    <div className="flex items-center gap-3 rounded-lg border border-zinc-200 bg-card p-3 shadow-sm dark:border-zinc-800">
      <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400">
        <Lock className="size-4" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-foreground">
          {block.motivo}
        </p>
        <p className="truncate text-xs text-muted-foreground">
          {formatDateShort(block.fechaInicio)} — {formatDateShort(block.fechaFin)}
        </p>
      </div>

      <StatusBadge status={state} configMap={BLOCK_STATE_CONFIG} />

      <Button
        variant="ghost"
        size="icon-sm"
        onClick={() => onEdit(block)}
        className="shrink-0 cursor-pointer"
        aria-label="Editar bloqueo"
      >
        <Pencil className="size-4" />
      </Button>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon-sm"
            className="shrink-0 cursor-pointer"
            aria-label="Acciones del bloqueo"
          >
            <Power className="size-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          {block.activo ? (
            <DropdownMenuItem
              variant="destructive"
              onClick={() => onDeactivate(block)}
              className="text-red-600"
            >
              <Power className="mr-2 size-4" />
              Desactivar
            </DropdownMenuItem>
          ) : canReactivateBlock(block) ? (
            <DropdownMenuItem onClick={() => onReactivate(block)}>
              <RefreshCw className="mr-2 size-4" />
              Activar
            </DropdownMenuItem>
          ) : null}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}