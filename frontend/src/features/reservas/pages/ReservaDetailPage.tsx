import { useParams, useNavigate, useSearchParams, useLocation } from "react-router-dom";
import { useState, useEffect, useRef } from "react";
import { XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import DetailPageLayout from "@/layouts/dashboard/DetailPageLayout";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import {
  useReservaDetalle,
  useCheckInConHuespedes,
  useCheckOutDetalle,
  useDeleteReserva,
  useActualizarReserva,
} from "../hooks/useReservaDetalle";
import { useBuscarCliente } from "../hooks/useBuscarCliente";
import ReservaDetalleInfoHeader from "../components/details/ReservaDetalleInfoHeader";
import ReservaGeneralInfo from "../components/details/ReservaGeneralInfo";
import ReservaTitularSection from "../components/details/ReservaTitularSection";
import ReservaHabitacionesHuespedes from "../components/details/ReservaHabitacionesHuespedes";
import ReservaPagosSection from "../components/details/ReservaPagosSection";
import ReservaConsumosSection from "../components/details/ReservaConsumosSection";
import CheckInModal from "../components/details/CheckInModal";
import type { DetalleReservaResponse } from "@/api/generated/openAPIDefinition.schemas";

function ReservaDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const location = useLocation();
  const numId = id ? Number(id) : undefined;

  const { data: reserva, isLoading } = useReservaDetalle(numId);
  const pagosRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isLoading || !reserva) return;
    const focus = searchParams.get("focus");
    const hash = location.hash;
    if (focus === "pagos" || hash === "#pagos") {
      // esperar a que el DOM pinte
      setTimeout(() => {
        pagosRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
      }, 300);
    }
  }, [isLoading, reserva, searchParams, location.hash]);
  const { data: clientesData } = useBuscarCliente();

  const checkInConHuespedesMut = useCheckInConHuespedes();
  const checkOutDetalleMut = useCheckOutDetalle();
  const deleteMutation = useDeleteReserva();
  const cancelMutation = useActualizarReserva();

  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [checkInDetalle, setCheckInDetalle] =
    useState<DetalleReservaResponse | null>(null);

  // ── Per-detalle ──

  const handleCheckInClick = (detalle: DetalleReservaResponse) => {
    setCheckInDetalle(detalle);
  };

  const handleCheckInConfirm = (detalleId: number, clienteIds: number[]) => {
    if (!id) return;
    checkInConHuespedesMut.mutate(
      { reservaId: Number(id), detalleId, clienteIds },
      { onSuccess: () => setCheckInDetalle(null) },
    );
  };

  const handleCheckOut = (detalleId: number) => {
    if (!id) return;
    checkOutDetalleMut.mutate({ reservaId: Number(id), detalleId });
  };

  // ── Cancel ──

  const handleCancel = () => {
    if (id) {
      cancelMutation.mutate(
        { id: Number(id), data: { estado: "CANCELADA" as const } },
        { onSuccess: () => setShowCancelDialog(false) },
      );
    }
  };

  // ── Delete ──

  const handleDelete = () => {
    if (id) {
      deleteMutation.mutate(Number(id), {
        onSuccess: () => navigate("/dashboard/reservas"),
      });
    }
  };

  if (isLoading) {
    return (
      <BackHeaderLayout title="Reservas" subtitle="Detalle de reserva">
        <DetailSkeleton cards={4} />
      </BackHeaderLayout>
    );
  }

  if (!reserva) {
    return (
      <BackHeaderLayout title="Reservas" subtitle="Detalle de reserva">
        <NotFoundState
          title="Reserva no encontrada"
          description="La reserva que buscas no existe o fue eliminada."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout title="Reservas" subtitle="Detalle de reserva">
      <DetailPageLayout
        onEdit={() => navigate(`/dashboard/reservas/${id}/editar`)}
        onDelete={() => setShowDeleteDialog(true)}
        actions={
          (reserva.estado === "PENDIENTE" || reserva.estado === "CONFIRMADA") && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowCancelDialog(true)}
              disabled={cancelMutation.isPending || reserva.estado === "CONFIRMADA"}
              className="gap-1.5 text-red-600 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-950/20"
            >
              <XCircle className="w-4 h-4" />
              <span className="hidden sm:inline">Cancelar reserva</span>
            </Button>
          )
        }
        identity={
          <ReservaDetalleInfoHeader
            codigoReserva={reserva.codigoReserva}
            estado={reserva.estado}
          />
        }>
        <ReservaGeneralInfo
          fechaCreacion={reserva.fechaCreacion}
          fechaCheckIn={reserva.fechaCheckIn}
          fechaCheckOut={reserva.fechaCheckOut}
          horaCheckIn={reserva.horaCheckIn}
          horaCheckOut={reserva.horaCheckOut}
          noches={reserva.noches}
          total={reserva.total}
          origen={reserva.origen}
          observaciones={reserva.observaciones}
        />

        <div className="h-px bg-zinc-100 dark:bg-zinc-800" />

        <ReservaTitularSection titular={reserva.titular} />

        <div className="h-px bg-zinc-100 dark:bg-zinc-800" />

        <ReservaHabitacionesHuespedes
          detalles={reserva.detalles}
          clientes={clientesData?.content}
          onCheckInClick={handleCheckInClick}
          onCheckOut={handleCheckOut}
          isCheckingIn={checkInConHuespedesMut.isPending}
          isCheckingOut={checkOutDetalleMut.isPending}
        />

        <div className="h-px bg-zinc-100 dark:bg-zinc-800" />

        <div ref={pagosRef} id="pagos" className="scroll-mt-24 space-y-6">
          <ReservaConsumosSection reservaId={numId!} />
          <ReservaPagosSection
            reservaId={numId!}
            total={reserva.total ?? 0}
            desglose={reserva.desglose}
            totalPagado={reserva.totalPagado}
            saldoPendiente={reserva.saldoPendiente}
            ajustes={reserva.ajustes}
            pagos={reserva.pagos}
          />
        </div>
      </DetailPageLayout>

      {checkInDetalle && (
        <CheckInModal
          open
          onClose={() => setCheckInDetalle(null)}
          detalle={checkInDetalle}
          huespedesActuales={checkInDetalle.huespedes}
          onConfirm={handleCheckInConfirm}
          isPending={checkInConHuespedesMut.isPending}
        />
      )}

      <ConfirmDeleteDialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={handleDelete}
        isPending={deleteMutation.isPending}
        title="Eliminar reserva"
        description={`¿Estás seguro de eliminar la reserva "${reserva.codigoReserva ?? reserva.id}"? Esta acción no se puede deshacer.`}
      />

      <ConfirmDeleteDialog
        open={showCancelDialog}
        onClose={() => setShowCancelDialog(false)}
        onConfirm={handleCancel}
        isPending={cancelMutation.isPending}
        title="Cancelar reserva"
        description={`¿Estás seguro de cancelar la reserva "${reserva.codigoReserva ?? reserva.id}"? El total quedará en $0 y los pagos realizados se conservan.`}
        confirmLabel="Cancelar reserva"
      />
    </BackHeaderLayout>
  );
}

export default ReservaDetailPage;
