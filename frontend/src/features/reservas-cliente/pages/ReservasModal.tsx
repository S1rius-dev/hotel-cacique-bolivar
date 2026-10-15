import { useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";
import { useAuth } from "@/app/auth/AuthContext";
import { toast } from "sonner";
import ReservaCard from "../components/ReservaCard";
import EmptyReservas from "../components/EmptyReservas";
import ReservaCardSkeleton from "../components/ReservaCardSkeleton";
import type { ReservaData } from "../components/ReservaCard";
import { AlertCircle, RefreshCw } from "lucide-react";
import type { MisReservasItem } from "../api/mis-reservas.api";
import {
  useMisReservas,
  useCancelarReserva,
  useEditarFechasReserva,
} from "../hooks/useMisReservas";
import { formatDateShort } from "@/utils/date";
import { extractErrorMessage } from "@/lib/extract-error";

function mapToReservaData(item: MisReservasItem): ReservaData {
  return {
    id: item.id,
    codigoReserva: item.codigoReserva,
    fechaCheckIn: item.fechaCheckIn,
    fechaCheckOut: item.fechaCheckOut,
    noches: item.noches,
    estado: item.estado as ReservaData["estado"],
    total: item.total,
    habitaciones: item.habitaciones?.map((h) => ({
      tipo: h.tipoHabitacion ?? h.numeroHabitacion ?? "Habitación",
      cantidad: h.cantidadOcupantes ?? 1,
    })) ?? [],
  };
}

export default function ReservasModal() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const redirecting = useRef(false);

  const open = location.pathname === "/mis-reservas";
  const clienteId = user?.clienteId;
  const showProfilePrompt = open && !!user && !clienteId;

  const {
    data: items,
    isLoading,
    isError,
    error,
    refetch,
  } = useMisReservas(open && !!clienteId);

  const cancelar = useCancelarReserva();
  const editarFechas = useEditarFechasReserva();

  useEffect(() => {
    if (open && !user && !redirecting.current) {
      redirecting.current = true;
      toast.info("Debes iniciar sesión primero");
      navigate("/", { replace: true, state: { openLogin: true } });
    }
    if (!open) {
      redirecting.current = false;
    }
  }, [open, user, navigate]);

  const handleClose = () => {
    if (location.state?.background) {
      navigate(location.state.background.pathname, { replace: true });
    } else {
      navigate("/", { replace: true });
    }
  };

  const handleCancel = (id: number) => {
    if (!id) return;
    cancelar.mutate(id);
  };

  const handleEditFechas = (
    id: number,
    checkIn: string,
    checkOut: string,
  ) => {
    if (!id) return;
    editarFechas.mutate({ id, fechaCheckIn: checkIn, fechaCheckOut: checkOut });
  };

  const handlePagar = (reserva: ReservaData) => {
    if (!reserva.id) return;
    navigate(`/pago?reserva=${reserva.id}`, {
      state: {
        reservaId: reserva.id,
        codigoReserva: reserva.codigoReserva ?? "",
        checkIn: reserva.fechaCheckIn
          ? formatDateShort(reserva.fechaCheckIn)
          : undefined,
        checkOut: reserva.fechaCheckOut
          ? formatDateShort(reserva.fechaCheckOut)
          : undefined,
        nights: reserva.noches,
        totalPrice: reserva.total,
        extraPersonCharge: 0,
        habitaciones: reserva.habitaciones,
      },
    });
  };

  if (open && !user) return null;

  const reservas = (items ?? []).map(mapToReservaData);
  const errorMessage = extractErrorMessage(error, "Error al cargar reservas");

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <PublicDialogContent className="w-[95vw] sm:max-w-4xl max-h-[85vh] overflow-y-auto rounded-2xl p-0 gap-0">
        <div className="px-6 pt-6 pb-2">
          <DialogHeader>
            <DialogTitle>Mis Reservas</DialogTitle>
            <DialogDescription>
              Solo se muestran reservas activas y vigentes (pendientes,
              confirmadas o en curso)
            </DialogDescription>
          </DialogHeader>
        </div>

        <div className="px-6 pb-6">
          {showProfilePrompt ? (
            <div className="flex flex-col items-center gap-3 rounded-xl border border-amber-200 bg-amber-50 p-6 text-center sm:col-span-2">
              <AlertCircle className="size-8 text-amber-500" />
              <p className="text-sm text-amber-700">
                Debes completar tu perfil de cliente para ver tus reservas.
              </p>
              <button
                type="button"
                onClick={() =>
                  navigate("/mi-perfil", {
                    state: {
                      background: location.state?.background ?? {
                        pathname: "/",
                      },
                    },
                  })
                }
                className="rounded-xl bg-amber-700 px-5 py-2 text-sm font-medium text-white hover:bg-amber-800 cursor-pointer"
              >
                Ir a mi perfil
              </button>
            </div>
          ) : isLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <ReservaCardSkeleton />
              <ReservaCardSkeleton />
            </div>
          ) : isError ? (
            <div className="flex flex-col items-center gap-3 rounded-xl border border-red-200 bg-red-50 p-6 text-center sm:col-span-2">
              <AlertCircle className="size-8 text-red-500" />
              <p className="text-sm text-red-700">{errorMessage}</p>
              <button
                type="button"
                onClick={() => void refetch()}
                className="inline-flex items-center gap-2 rounded-xl bg-red-600 px-5 py-2 text-sm font-medium text-white hover:bg-red-700 cursor-pointer"
              >
                <RefreshCw className="size-4" />
                Reintentar
              </button>
            </div>
          ) : reservas.length === 0 ? (
            <div className="sm:col-span-2">
              <EmptyReservas />
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {reservas.map((reserva) => (
                <ReservaCard
                  key={reserva.id}
                  reserva={reserva}
                  onCancel={handleCancel}
                  onEditFechas={handleEditFechas}
                  onPagar={handlePagar}
                  isCancelling={
                    cancelar.isPending && cancelar.variables === reserva.id
                  }
                  isEditing={
                    editarFechas.isPending &&
                    editarFechas.variables?.id === reserva.id
                  }
                />
              ))}
            </div>
          )}
        </div>
      </PublicDialogContent>
    </Dialog>
  );
}
