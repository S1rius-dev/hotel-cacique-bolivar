import { BedDouble, User } from "lucide-react";
import type { ReservaActiva } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";

interface Props {
  reserva: ReservaActiva;
  estadoBadge?: { label: string; className: string } | null;
}

function RoomReservaCard({ reserva, estadoBadge }: Props) {
  return (
    <div className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-3 space-y-2">
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground flex items-center gap-1.5">
          <BedDouble className="w-3 h-3" />
          Reserva
        </p>
        {estadoBadge && (
          <span
            className={`text-[11px] px-2 py-0.5 rounded-full border font-medium ${estadoBadge.className}`}>
            {estadoBadge.label}
          </span>
        )}
      </div>

      <div className="space-y-1.5 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">Código</span>
          <span className="font-medium font-mono">{reserva.codigoReserva}</span>
        </div>
        {reserva.titularNombre && (
          <div className="flex justify-between">
            <span className="text-muted-foreground">Titular</span>
            <span className="font-medium flex items-center gap-1">
              <User className="w-3 h-3" />
              {reserva.titularNombre}
            </span>
          </div>
        )}
        <div className="flex justify-between">
          <span className="text-muted-foreground">Check-in</span>
          <span className="font-medium">{formatDate(reserva.checkIn)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Check-out</span>
          <span className="font-medium">{formatDate(reserva.checkOut)}</span>
        </div>
      </div>
    </div>
  );
}

export default RoomReservaCard;
