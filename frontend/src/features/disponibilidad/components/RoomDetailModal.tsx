import { BedDouble, Building2, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  ESTADO_HABITACION_STYLES,
} from "../constants/disponibilidad.constants";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";
import RoomReservaCard from "./RoomReservaCard";
import RoomPagosCard from "./RoomPagosCard";
import RoomHuespedesCard from "./RoomHuespedesCard";

interface Props {
  open: boolean;
  habitacion: HabitacionEstadoDiario | null;
  onClose: () => void;
  onNavigate: (reservaId: number) => void;
  onVender?: (habitacion: HabitacionEstadoDiario) => void;
}

function RoomDetailModal({ open, habitacion, onClose, onNavigate, onVender }: Props) {
  if (!open || !habitacion) return null;

  const badge = ESTADO_HABITACION_STYLES[habitacion.estado ?? "DISPONIBLE"] ?? {
    label: habitacion.estado ?? "DISPONIBLE",
    className: "bg-zinc-100 text-zinc-700",
  };

  const huespedes = habitacion.reservaActiva?.huespedes ?? [];
  const reserva = habitacion.reservaActiva;

  return (
    <>
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-start justify-between pr-6">
            <DialogTitle className="flex items-center gap-2">
              <BedDouble className="w-5 h-5 text-zinc-600" />
              Habitación {habitacion.numero}
            </DialogTitle>
            <span
              className={`shrink-0 text-[11px] px-2 py-0.5 rounded-full border font-medium ${badge.className}`}>
              {badge.label}
            </span>
          </div>
          <DialogDescription>
            <Building2 className="w-3 h-3 inline mr-1" />
            {habitacion.tipoNombre} · {habitacion.capacidadBase}-
            {habitacion.capacidadMaxima} Máx
          </DialogDescription>
        </DialogHeader>

        {reserva && (
          <div className="grid grid-cols-2 gap-4 py-2">
            <RoomReservaCard
              reserva={reserva}
              estadoBadge={badge}
            />

            <RoomPagosCard
              reservaId={reserva.reservaId!}
              total={reserva.total ?? 0}
              totalPagado={reserva.totalPagado ?? 0}
              saldoPendiente={reserva.saldoPendiente ?? 0}
            />
          </div>
        )}

        <RoomHuespedesCard huespedes={huespedes} />

        <DialogFooter className="justify-between gap-2 flex-wrap">
          <Button variant="ghost" onClick={onClose}>
            Cerrar
          </Button>

          <div className="flex gap-2">
            {reserva?.estadoReserva === "EN_CURSO" && onVender && habitacion && (
              <Button variant="outline" onClick={() => { onVender(habitacion); onClose(); }} className="gap-2 border-rose-200 text-rose-700 hover:bg-rose-50">
                <span className="hidden sm:inline">Vender producto</span>
                <span className="sm:hidden">Vender</span>
              </Button>
            )}
            {reserva && (
              <Button
                variant="default"
                onClick={() => onNavigate(reserva.reservaId!)}
                className="gap-2">
                <ExternalLink className="w-4 h-4" />
                Ir a detalle de la reserva
              </Button>
            )}
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
    </>
  );
}

export default RoomDetailModal;
