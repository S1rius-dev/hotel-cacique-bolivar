import { useState, useMemo } from "react";
import { CalendarDays, Moon, DollarSign } from "lucide-react";
import { formatDate } from "@/utils/date";
import { formatCurrency } from "@/utils/format";
import TimelineEstado from "./TimelineEstado";
import ReservaActions from "./ReservaActions";
import CancelReservaDialog from "./CancelReservaDialog";
import EditFechasDialog from "./EditFechasDialog";

export type AccionEstado = "activo" | "proximo" | "vencido" | "no_disponible";

export interface AccionesEvaluadas {
  cancelar: AccionEstado;
  editar: AccionEstado;
  mensaje?: string;
}

export interface ReservaData {
  id?: number;
  codigoReserva?: string;
  fechaCheckIn?: string;
  fechaCheckOut?: string;
  noches?: number;
  estado?: "PENDIENTE" | "PENDIENTE_PAGO" | "CONFIRMADA" | "EN_CURSO" | "FINALIZADA" | "CANCELADA" | "NO_SHOW";
  total?: number;
  habitaciones?: { tipo?: string; cantidad?: number }[];
}

interface Props {
  reserva: ReservaData;
  onCancel: (id: number) => void;
  onEditFechas: (id: number, checkIn: string, checkOut: string) => void;
  onPagar?: (reserva: ReservaData) => void;
  isCancelling?: boolean;
  isEditing?: boolean;
}

function evaluarAcciones(estado?: string, checkIn?: string): AccionesEvaluadas {
  const noDisponible = { cancelar: "no_disponible" as AccionEstado, editar: "no_disponible" as AccionEstado };

  if (estado !== "PENDIENTE" && estado !== "PENDIENTE_PAGO" && estado !== "CONFIRMADA") return noDisponible;
  if (!checkIn) return noDisponible;

  const diffMs = new Date(checkIn).getTime() - Date.now();
  const diffHours = diffMs / (1000 * 60 * 60);

  if (diffHours < 0) {
    return {
      cancelar: "vencido",
      editar: "vencido",
      mensaje:
        "La fecha de check-in ya pasó y no se realizó el ingreso. " +
        "Por favor, comunícate directamente con el hotel al +57 300 0000000 para regularizar tu situación.",
    };
  }

  if (diffHours < 48) {
    return {
      cancelar: "proximo",
      editar: "proximo",
      mensaje:
        "Tu reserva está próxima a iniciar. Para modificaciones o cancelaciones de último momento, " +
        "comunícate directamente con el hotel al +57 300 0000000.",
    };
  }

  return { cancelar: "activo", editar: "activo" };
}

export default function ReservaCard({
  reserva,
  onCancel,
  onEditFechas,
  onPagar,
  isCancelling,
  isEditing,
}: Props) {
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);

  const acciones = useMemo(
    () => evaluarAcciones(reserva.estado, reserva.fechaCheckIn),
    [reserva.estado, reserva.fechaCheckIn],
  );

  const isPast =
    reserva.estado === "FINALIZADA" ||
    reserva.estado === "CANCELADA" ||
    reserva.estado === "NO_SHOW";

  const canPay = reserva.estado === "PENDIENTE_PAGO" && !isPast && !!reserva.id && !!onPagar;

  const handleCancel = () => {
    if (reserva.id) onCancel(reserva.id);
    setShowCancelDialog(false);
  };

  const handleEditFechas = (checkIn: string, checkOut: string) => {
    if (reserva.id) onEditFechas(reserva.id, checkIn, checkOut);
    setShowEditDialog(false);
  };

  return (
    <>
      <div className="group rounded-xl border border-stone-200 bg-white transition-all duration-200 hover:border-rose-200 hover:shadow-sm">
        <div className="p-5">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-mono text-xs font-semibold text-stone-400 uppercase tracking-wider">
                {reserva.codigoReserva ?? `#${reserva.id}`}
              </p>
              {reserva.habitaciones && reserva.habitaciones.length > 0 && (
                <p className="mt-1 text-sm font-medium text-stone-700 truncate">
                  {reserva.habitaciones.map((h) => h.tipo ?? "Habitación").join(", ")}
                </p>
              )}
            </div>
            <TimelineEstado estado={reserva.estado ?? "PENDIENTE"} />
          </div>

          <div className="mt-4 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
            <div className="flex items-center gap-2 text-stone-500">
              <CalendarDays className="size-3.5 shrink-0 text-rose-400" />
              <span>
                {formatDate(reserva.fechaCheckIn)} → {formatDate(reserva.fechaCheckOut)}
              </span>
            </div>
            <div className="flex items-center gap-2 text-stone-500">
              <Moon className="size-3.5 shrink-0 text-rose-400" />
              <span>{reserva.noches ?? "—"} noche(s)</span>
            </div>
            {reserva.total != null && (
              <div className="flex items-center gap-2 text-stone-500 col-span-2">
                <DollarSign className="size-3.5 shrink-0 text-rose-400" />
                <span className="font-medium text-stone-700">
                  {formatCurrency(reserva.total)} COP
                </span>
              </div>
            )}
          </div>

          {acciones.mensaje && (
            <div
              className={`mt-3 rounded-lg border p-3 text-xs leading-relaxed ${
                acciones.cancelar === "vencido"
                  ? "border-red-200 bg-red-50 text-red-700"
                  : "border-amber-200 bg-amber-50 text-amber-700"
              }`}
            >
              <p>{acciones.mensaje}</p>
            </div>
          )}

          <ReservaActions
            acciones={acciones}
            onCancel={() => setShowCancelDialog(true)}
            onEdit={() => setShowEditDialog(true)}
            onPay={canPay ? () => onPagar?.(reserva) : undefined}
            isPast={isPast}
          />
        </div>
      </div>

      <CancelReservaDialog
        open={showCancelDialog}
        onOpenChange={setShowCancelDialog}
        onConfirm={handleCancel}
        isPending={isCancelling}
        codigoReserva={reserva.codigoReserva}
      />

      <EditFechasDialog
        open={showEditDialog}
        onOpenChange={setShowEditDialog}
        onConfirm={handleEditFechas}
        isPending={isEditing}
        currentCheckIn={reserva.fechaCheckIn}
        currentCheckOut={reserva.fechaCheckOut}
      />
    </>
  );
}
