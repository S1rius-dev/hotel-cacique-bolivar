import { useEffect } from "react";
import { X, CalendarDays, Hotel, DollarSign, Loader2, CheckCircle2, AlertCircle, Camera, Users } from "lucide-react";
import type { BookingRoomType } from "@/features/booking/types";
import { getRoomImages } from "@/components/home/rooms.data";
import type { CustomerReservationResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Selection {
  tipo: BookingRoomType;
  quantity: number;
}

interface Props {
  open: boolean;
  onClose: () => void;
  checkIn: string;
  checkOut: string;
  nights: number;
  selections: Selection[];
  totalRooms: number;
  totalGuests?: number;
  onRetry: () => void;
  loading: boolean;
  result: CustomerReservationResponse | null;
  error: string | null;
}

export default function BookingConfirmModal({
  open,
  onClose,
  checkIn,
  checkOut,
  nights,
  selections,
  totalRooms,
  totalGuests,
  onRetry,
  loading,
  result,
  error,
}: Props) {
  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
      return () => { document.body.style.overflow = ""; };
    }
  }, [open]);

  if (!open) return null;

  const formatPrice = (v: number) => `$${v.toLocaleString("es-CO")}`;
  const visibleSelections = selections.filter((s) => s.quantity > 0);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
      onClick={loading ? undefined : onClose}
    >
      <div
        className="relative w-full max-w-lg rounded-3xl bg-white shadow-2xl overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {loading && !result ? (
          /* Loading state */
          <div className="px-6 py-16 text-center">
            <Loader2 className="size-10 animate-spin text-rose-500 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-stone-800">
              Reservando...
            </h3>
            <p className="text-sm text-stone-400 mt-1">
              Verificando disponibilidad
            </p>
          </div>
        ) : result ? (
          /* Success state */
          <>
            <div className="bg-gradient-to-r from-emerald-700 to-emerald-600 px-6 py-6 text-center">
              <CheckCircle2 className="size-12 text-emerald-200 mx-auto mb-3" />
              <h2 className="text-xl font-bold text-white tracking-tight">
                ¡Reserva confirmada!
              </h2>
              <p className="text-emerald-100 text-sm mt-1">
                Tu código de reserva
              </p>
              <p className="text-2xl font-bold text-white mt-1 tracking-wider">
                {result.codigoReserva}
              </p>
            </div>

            <div className="px-6 py-5 space-y-5">
              <div className="flex items-center gap-3 pb-4 border-b border-stone-100">
                <CalendarDays className="size-5 text-emerald-500 shrink-0" />
                <div className="flex items-center gap-2 text-sm">
                  <span className="font-semibold text-stone-800">{checkIn}</span>
                  <span className="text-emerald-400">→</span>
                  <span className="font-semibold text-stone-800">{checkOut}</span>
                  <span className="text-stone-400 text-xs ml-1">
                    ({nights} noche{nights !== 1 ? "s" : ""})
                  </span>
                </div>
              </div>

              {totalGuests !== undefined && totalGuests > 0 && (
                <div className="flex items-center gap-2 text-xs text-stone-500">
                  <Users className="size-4" />
                  <span>{totalGuests} {totalGuests === 1 ? "huésped" : "huéspedes"}</span>
                </div>
              )}

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-xs uppercase tracking-wider text-stone-400 font-medium">
                  <Hotel className="size-4" />
                  Habitaciones asignadas
                </div>
                {result.habitaciones?.map((h) => (
                  <div
                    key={h.id}
                    className="flex items-center justify-between py-2"
                  >
                    <div className="flex items-center gap-3">
                      <div className="size-10 rounded-lg bg-stone-100 flex items-center justify-center">
                        <Hotel className="size-5 text-stone-400" />
                      </div>
                      <div>
                        <p className="text-sm font-medium text-stone-700">
                          Hab. {h.numeroHabitacion}
                        </p>
                        <p className="text-xs text-stone-400">
                          {h.cantidadOcupantes} huésped{h.cantidadOcupantes !== 1 ? "es" : ""} · {formatPrice(h.precioPorNoche ?? 0)}/noche
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="pt-4 border-t border-stone-100">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-sm text-stone-500">
                    <DollarSign className="size-4" />
                    Total
                  </div>
                  <span className="text-2xl font-bold text-stone-900 tabular-nums">
                    {formatPrice(result.total ?? 0)}
                  </span>
                </div>
              </div>

              <button
                type="button"
                onClick={onClose}
                className="w-full px-6 py-3 rounded-xl bg-stone-100 hover:bg-stone-200 text-stone-700 font-semibold text-sm transition-all cursor-pointer"
              >
                Cerrar
              </button>
            </div>
          </>
        ) : (
          /* Error state */
          <>
            <div className="bg-gradient-to-r from-red-700 to-red-600 px-6 py-5">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-white tracking-tight">
                  Error al reservar
                </h2>
                <button
                  type="button"
                  onClick={onClose}
                  className="p-1 rounded-full text-red-200 hover:text-white hover:bg-white/10 transition-all cursor-pointer"
                >
                  <X className="size-5" />
                </button>
              </div>
            </div>

            <div className="px-6 py-5 space-y-5">
              <div className="flex items-start gap-3 p-4 rounded-xl bg-red-50 border border-red-200">
                <AlertCircle className="size-5 text-red-500 shrink-0 mt-0.5" />
                <p className="text-sm text-red-700">{error}</p>
              </div>

              <div className="flex items-center gap-3 pb-4 border-b border-stone-100">
                <CalendarDays className="size-5 text-red-400 shrink-0" />
                <div className="flex items-center gap-2 text-sm">
                  <span className="font-semibold text-stone-800">{checkIn}</span>
                  <span className="text-stone-300">→</span>
                  <span className="font-semibold text-stone-800">{checkOut}</span>
                  <span className="text-stone-400 text-xs ml-1">
                    ({nights} noche{nights !== 1 ? "s" : ""})
                  </span>
                </div>
              </div>

              {totalGuests !== undefined && totalGuests > 0 && (
                <div className="flex items-center gap-2 text-xs text-stone-500">
                  <Users className="size-4" />
                  <span>{totalGuests} {totalGuests === 1 ? "huésped" : "huéspedes"}</span>
                </div>
              )}

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-xs uppercase tracking-wider text-stone-400 font-medium">
                  <Hotel className="size-4" />
                  Habitaciones ({totalRooms})
                </div>
                {visibleSelections.map((sel) => (
                  <div
                    key={sel.tipo.id}
                    className="flex items-center justify-between py-2"
                  >
                    <div className="flex items-center gap-3">
                      <div className="size-10 rounded-lg bg-stone-100 flex items-center justify-center overflow-hidden">
                        {getRoomImages(sel.tipo.nombre) ? (
                          <img
                            src={getRoomImages(sel.tipo.nombre)![0]}
                            alt={sel.tipo.nombre}
                            className="size-full object-cover"
                          />
                        ) : (
                          <Camera className="size-5 text-stone-400" />
                        )}
                      </div>
                      <div>
                        <p className="text-sm font-medium text-stone-700">
                          {sel.tipo.nombre}
                        </p>
                        <p className="text-xs text-stone-400">
                          x{sel.quantity} · {formatPrice(sel.tipo.precioBase)}/noche
                        </p>
                      </div>
                    </div>
                    <span className="text-sm font-semibold text-stone-800 tabular-nums">
                      {formatPrice(sel.tipo.precioBase * sel.quantity * nights)}
                    </span>
                  </div>
                ))}
              </div>

              <button
                type="button"
                onClick={onRetry}
                className="w-full px-6 py-3.5 rounded-xl bg-rose-700 hover:bg-rose-800 text-white font-semibold text-sm transition-all cursor-pointer shadow-lg shadow-rose-900/20 hover:shadow-xl"
              >
                Intentar de nuevo
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
