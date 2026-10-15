import {
  BedDouble,
  CheckCircle,
  LogOut,
  Loader2,
  BadgeCheck,
  Phone,
  Fingerprint,
  ExternalLink,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { formatCurrency } from "@/utils/format";
import { DETALLE_ESTADO_STYLES } from "../../constants/reserva.constants";
import type {
  DetalleReservaResponse,
  ClienteResponse,
} from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  detalles?: DetalleReservaResponse[];
  onCheckInClick?: (detalle: DetalleReservaResponse) => void;
  onCheckOut?: (detalleId: number) => void;
  isCheckingIn?: boolean;
  isCheckingOut?: boolean;
  clientes?: ClienteResponse[];
}

function ReservaHabitacionesHuespedes({
  detalles,
  onCheckInClick,
  onCheckOut,
  isCheckingIn,
  isCheckingOut,
  clientes,
}: Props) {
  const navigate = useNavigate();
  if (!detalles || detalles.length === 0) {
    return (
      <div className="space-y-4">
        <h2 className="text-sm font-sans font-semibold text-zinc-900 dark:text-zinc-100 uppercase tracking-wide">
          Habitaciones y Huéspedes
        </h2>

        <p className="text-sm text-muted-foreground">
          No hay habitaciones registradas
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-sm font-sans font-semibold text-zinc-900 dark:text-zinc-100 uppercase tracking-wide">
        Habitaciones y Huéspedes
      </h2>

      <div className="space-y-4">
        {detalles.map((detalle) => {
          const badge = detalle.estado
            ? (DETALLE_ESTADO_STYLES[detalle.estado] ?? {
                label: detalle.estado,
                className: "bg-zinc-100 text-zinc-700 border-zinc-200",
              })
            : null;

          const puedeCheckIn =
            detalle.estado === "RESERVADA" ||
            (detalle.estado === "OCUPADA" &&
              detalle.huespedes != null &&
              detalle.cantidadOcupantes != null &&
              detalle.huespedes.length < detalle.cantidadOcupantes);
          const puedeCheckOut = detalle.estado === "OCUPADA";

          return (
            <div
              key={detalle.id}
              className="rounded-lg border bg-white dark:bg-zinc-900/60 p-4 space-y-3">
              <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                <div className="flex items-start gap-3 min-w-0">
                  <BedDouble className="w-4 h-4 text-muted-foreground mt-1 shrink-0" />

                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                      Habitación {detalle.habitacionNumero ?? "—"}
                    </p>

                    <div className="flex items-center gap-2 mt-1 flex-wrap">
                      {badge && (
                        <span
                          className={`inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full border font-medium ${badge.className}`}>
                          {badge.label}
                        </span>
                      )}

                      {detalle.precioPorNoche != null && (
                        <span className="text-[11px] text-muted-foreground">
                          {formatCurrency(detalle.precioPorNoche)} / noche
                        </span>
                      )}

                      {detalle.cantidadOcupantes != null && (
                        <span className="text-[11px] text-muted-foreground">
                          {detalle.cantidadOcupantes} ocupante(s)
                        </span>
                      )}

                      {detalle.subtotal != null && (
                        <span className="text-[11px] font-medium text-zinc-700 dark:text-zinc-300">
                          Subtotal: {formatCurrency(detalle.subtotal)}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0 flex-wrap">
                  {puedeCheckIn && onCheckInClick && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onCheckInClick(detalle)}
                      disabled={isCheckingIn}
                      className="text-green-700 border-green-200 hover:bg-green-50">
                      <CheckCircle className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">{detalle.estado === "RESERVADA"
                        ? "Registrar entrada"
                        : "Agregar huésped"}</span>
                    </Button>
                  )}

                  {puedeCheckOut && onCheckOut && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onCheckOut(detalle.id!)}
                      disabled={isCheckingOut}
                      className="text-orange-700 border-orange-200 hover:bg-orange-50">
                      {isCheckingOut ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <LogOut className="w-3.5 h-3.5" />
                      )}
                      <span className="hidden sm:inline">Registrar salida</span>
                    </Button>
                  )}
                </div>
              </div>

              {detalle.huespedes && detalle.huespedes.length > 0 && (
                <div className="ml-7 pl-3 border-l-2 border-zinc-200 dark:border-zinc-700 space-y-3">
                  <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                    Huéspedes
                  </p>

                  {detalle.huespedes.map((huesped) => {
                    const cliente = clientes?.find(
                      (c) => c.id === huesped.huespedId,
                    );

                    return (
                      <div
                        key={huesped.detalleHuespedId ?? huesped.huespedId}
                        className="flex items-start gap-4 p-3 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40">
                        <div className="min-w-0 flex-1 space-y-2">
                          <div className="flex items-start justify-between gap-2">
                            <div className="min-w-0">
                              <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                                Nombre completo
                              </p>
                              <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate">
                                {cliente
                                  ? `${cliente.nombreCompleto ?? ""} ${cliente.apellidoCompleto ?? ""}`
                                  : `${huesped.nombreCompleto ?? ""} ${huesped.apellidoCompleto ?? ""}`.trim() ||
                                    "—"}
                              </p>
                            </div>
                            {cliente?.id != null && (
                              <button
                                onClick={() => navigate(`/dashboard/clientes/${cliente.id}`)}
                                className="p-1 rounded-md hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-300 transition-colors shrink-0"
                                title="Ver detalle del cliente"
                              >
                                <ExternalLink className="w-3.5 h-3.5" />
                              </button>
                            )}
                          </div>

                          <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                            {(cliente?.tipoIdentificacion ||
                              huesped?.tipoIdentificacion) && (
                              <div className="flex items-start gap-1.5">
                                <BadgeCheck className="w-3.5 h-3.5 text-muted-foreground mt-0.5 shrink-0" />
                                <div>
                                  <p className="text-[10px] uppercase tracking-wide text-muted-foreground">
                                    Tipo ID
                                  </p>
                                  <p className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
                                    {typeof cliente?.tipoIdentificacion ===
                                    "string"
                                      ? cliente.tipoIdentificacion
                                      : (cliente?.tipoIdentificacion?.nombre ??
                                        huesped.tipoIdentificacion ??
                                        "—")}
                                  </p>
                                </div>
                              </div>
                            )}

                            {(cliente?.cedula || huesped?.cedula) && (
                              <div className="flex items-start gap-1.5">
                                <Fingerprint className="w-3.5 h-3.5 text-muted-foreground mt-0.5 shrink-0" />
                                <div>
                                  <p className="text-[10px] uppercase tracking-wide text-muted-foreground">
                                    Cédula
                                  </p>
                                  <p className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
                                    {cliente?.cedula ?? huesped.cedula}
                                  </p>
                                </div>
                              </div>
                            )}

                            {cliente?.telefono && (
                              <div className="flex items-start gap-1.5">
                                <Phone className="w-3.5 h-3.5 text-muted-foreground mt-0.5 shrink-0" />
                                <div>
                                  <p className="text-[10px] uppercase tracking-wide text-muted-foreground">
                                    Teléfono
                                  </p>
                                  <p className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
                                    {cliente.telefono}
                                  </p>
                                </div>
                              </div>
                            )}


                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              {(!detalle.huespedes || detalle.huespedes.length === 0) && (
                <div className="ml-7 pl-3 border-l-2 border-zinc-200 dark:border-zinc-700">
                  <p className="text-[11px] text-muted-foreground italic">
                    Sin huéspedes registrados
                  </p>
                </div>
              )}

              {detalle.observaciones && (
                <div className="ml-7 pl-3 border-l-2 border-zinc-200 dark:border-zinc-700">
                  <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                    Observaciones
                  </p>

                    <p className="text-sm text-zinc-700 dark:text-zinc-300 break-words">
                      {detalle.observaciones}
                    </p>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ReservaHabitacionesHuespedes;
