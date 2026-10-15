import { Calendar, ExternalLink, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { RESERVAS_STATUS_CONFIG } from "@/features/reservas/config/estados";
import { useReservasPorIds } from "@/features/reservas/hooks/useReservasPorIds";
import { useNavigate } from "react-router-dom";
import { formatCurrency } from "@/utils/format";

interface Props {
  reservaIds: number[];
}

function ClienteReservasSection({ reservaIds }: Props) {
  const navigate = useNavigate();
  const { reservas, isLoading } = useReservasPorIds(reservaIds);

  if (isLoading) {
    return (
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-blue-500">
        <div className="flex items-center gap-2 text-blue-600">
          <Loader2 className="w-4 h-4 animate-spin" />
          <p className="text-sm">Cargando reservas...</p>
        </div>
      </section>
    );
  }

  if (reservas.length === 0) {
    return (
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-blue-500">
        <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
          <Calendar className="h-5 w-5 text-blue-600" />
          Historial de Reservas
        </h3>
        <p className="text-sm text-muted-foreground">
          Este cliente no tiene reservas registradas.
        </p>
      </section>
    );
  }

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-blue-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <Calendar className="h-5 w-5 text-blue-600" />
        Historial de Reservas
        <span className="text-xs font-normal text-muted-foreground ml-1">
          ({reservas.length} reserva{reservas.length !== 1 ? "s" : ""})
        </span>
      </h3>

      <div className="space-y-3">
        {reservas.map((reserva) => (
          <div
            key={reserva.id}
            className="flex items-center gap-4 p-4 rounded-xl bg-white dark:bg-zinc-900 border border-border/60 shadow-sm"
          >
            <div className="flex-1 min-w-0 grid grid-cols-2 sm:grid-cols-4 gap-x-4 gap-y-1">
              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Código
                </p>
                <p className="text-sm font-semibold truncate">#{reserva.codigoReserva ?? reserva.id}</p>
              </div>

              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Check-In
                </p>
                <p className="text-sm truncate">{reserva.fechaCheckIn}</p>
              </div>

              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Check-Out
                </p>
                <p className="text-sm truncate">{reserva.fechaCheckOut}</p>
              </div>

              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Total
                </p>
                <p className="text-sm font-semibold tabular-nums truncate">
                  {formatCurrency(reserva.total)}
                </p>
              </div>

              <div className="col-span-2 sm:col-span-4 flex items-center justify-between gap-2 mt-1 pt-2 border-t border-border/30">
                <StatusBadge status={reserva.estado as string} configMap={RESERVAS_STATUS_CONFIG} />
                <Button
                  variant="outline"
                  size="sm"
                  className="gap-1.5 text-xs h-8"
                  onClick={() => navigate(`/dashboard/reservas/${reserva.id}`)}
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Ver detalle
                </Button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default ClienteReservasSection;
