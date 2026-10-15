import { useState, useEffect, useRef } from "react";
import { CalendarDays, CircleCheckBig, ArrowRight } from "lucide-react";
import type { BookingRoomType } from "@/features/booking/types";
import { roomAmenities } from "@/features/booking/constants/room-amenities";

interface Selection {
  tipo: BookingRoomType;
  quantity: number;
}

interface Props {
  checkIn: string;
  checkOut: string;
  nights: number;
  selections: Selection[];
  totalPrice: number;
  totalRooms: number;
  totalGuests?: number;
  extraPersonCharge?: number;
  allUnavailable?: boolean;
  isSubmitting?: boolean;
  onBook: () => void;
}

function formatCapacity(room: BookingRoomType): string {
  if (room.capacidadMaxima > room.capacidadBase) {
    return `${room.capacidadBase}-${room.capacidadMaxima} personas`;
  }
  return `${room.capacidadMaxima} ${room.capacidadMaxima === 1 ? "persona" : "personas"}`;
}

const trustBadges = [
  { text: "Pago seguro" },
  { text: "Confirmación inmediata" },
  { text: "Sin cargos ocultos" },
];

export default function BookingSummarySidebar({
  checkIn,
  checkOut,
  nights,
  selections,
  totalPrice,
  totalRooms,
  totalGuests,
  extraPersonCharge,
  allUnavailable = false,
  isSubmitting = false,
  onBook,
}: Props) {
  const formatPrice = (value: number) =>
    `$${value.toLocaleString("es-CO")}`;

  const visibleSelections = selections.filter((s) => s.quantity > 0);

  const [animatingId, setAnimatingId] = useState<string | null>(null);
  const prevSelectionsRef = useRef(selections);

  useEffect(() => {
    const prev = prevSelectionsRef.current;
    for (const sel of selections) {
      const prevSel = prev.find((s) => s.tipo.id === sel.tipo.id);
      if (sel.quantity > 0 && (!prevSel || prevSel.quantity !== sel.quantity)) {
        setAnimatingId(String(sel.tipo.id));
        const timer = setTimeout(() => setAnimatingId(null), 600);
        return () => clearTimeout(timer);
      }
    }
    prevSelectionsRef.current = selections;
  }, [selections]);

  return (
    <aside className="w-full rounded-2xl border border-stone-200 bg-white shadow-sm overflow-hidden">
      <div className="px-5 py-4 space-y-4">
        {/* Header */}
        <h3 className="text-sm font-semibold tracking-wide text-stone-800 uppercase">
          Tu reserva
        </h3>

        {/* Dates card */}
        <div className="bg-stone-50 rounded-xl border border-stone-200 p-4">
          <div className="flex items-center gap-2">
            <CalendarDays className="size-4 text-rose-500 shrink-0" />
            <span className="text-sm font-medium text-stone-800">
              {checkIn} → {checkOut}
            </span>
          </div>
          {(nights > 0 || (totalGuests !== undefined && totalGuests > 0)) && (
            <div className="flex items-center gap-2 mt-2 text-xs text-stone-500">
              {nights > 0 && <span>🌙 {nights} {nights === 1 ? "noche" : "noches"}</span>}
              {nights > 0 && totalGuests !== undefined && totalGuests > 0 && (
                <span className="text-stone-300">·</span>
              )}
              {totalGuests !== undefined && totalGuests > 0 && (
                <span>👤 {totalGuests} {totalGuests === 1 ? "huésped" : "huéspedes"}</span>
              )}
            </div>
          )}
        </div>

        {/* Room cards */}
        {visibleSelections.length > 0 && (
          <div className="space-y-3">
            {visibleSelections.map((sel) => (
              <div
                key={sel.tipo.id}
                className={`
                  bg-stone-50 rounded-xl border border-stone-200 p-4
                  transition-all duration-500
                  ${animatingId === String(sel.tipo.id) ? "ring-2 ring-rose-200 shadow-sm" : ""}
                `}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-stone-800 truncate">
                      {sel.tipo.nombre}
                    </p>
                    <p className="text-xs text-stone-500 mt-0.5">
                      👤 {formatCapacity(sel.tipo)}
                    </p>
                  </div>
                  <span className="text-xs text-stone-400 font-medium shrink-0 mt-0.5">
                    x{sel.quantity}
                  </span>
                </div>

                <div className="flex items-center gap-2 mt-2 text-stone-400">
                  {roomAmenities.slice(0, 5).map((amenity) => (
                    <amenity.icon key={amenity.label} className="size-3" />
                  ))}
                </div>

                <div className="flex items-center justify-between mt-2 pt-2 border-t border-stone-200/60">
                  <span className="text-xs text-stone-400">
                    {formatPrice(sel.tipo.precioNocheEfectivo ?? sel.tipo.precioBase)} / noche
                    {(sel.tipo.precioNocheEfectivo != null && sel.tipo.precioNocheEfectivo !== sel.tipo.precioBase) ? " · temporada" : ""}
                  </span>
                  <span className="text-sm font-semibold text-stone-800 tabular-nums">
                    {formatPrice((sel.tipo.precioNocheEfectivo ?? sel.tipo.precioBase) * sel.quantity * nights)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {visibleSelections.length === 0 && (
          <p className="text-xs text-stone-400 italic">
            Selecciona las habitaciones que deseas
          </p>
        )}

        {/* Totals card */}
        {visibleSelections.length > 0 && (
          <div className="bg-stone-50 rounded-xl border border-stone-200 p-4 space-y-1.5">
            {visibleSelections.map((sel) => (
              <div key={sel.tipo.id} className="flex items-center justify-between text-xs">
                <span className="text-stone-500 truncate mr-2">
                  {sel.tipo.nombre}{sel.quantity > 1 ? ` x${sel.quantity}` : ""}
                </span>
                <span className="text-stone-700 font-medium tabular-nums shrink-0">
                  {formatPrice((sel.tipo.precioNocheEfectivo ?? sel.tipo.precioBase) * sel.quantity * nights)}
                </span>
              </div>
            ))}
            <div className="flex items-center justify-between text-xs">
              <span className="text-stone-500">Personas extra</span>
              <span className="text-stone-500 tabular-nums">
                {extraPersonCharge != null && extraPersonCharge > 0
                  ? formatPrice(extraPersonCharge)
                  : "—"}
              </span>
            </div>
            <div className="border-t border-stone-200 pt-1.5" />
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-stone-700 uppercase tracking-wider">
                Total
              </span>
              <span className="text-lg font-bold text-stone-900 tabular-nums">
                {formatPrice(totalPrice + (extraPersonCharge ?? 0))} COP
              </span>
            </div>
          </div>
        )}

        {/* Trust badges */}
        <div className="space-y-1.5">
          {trustBadges.map((badge) => (
            <div key={badge.text} className="flex items-center gap-1.5 text-xs text-stone-500">
              <CircleCheckBig className="size-3.5 text-green-500 shrink-0" />
              {badge.text}
            </div>
          ))}
        </div>

        {/* Button */}
        <button
          type="button"
          disabled={totalRooms === 0 || allUnavailable || isSubmitting}
          onClick={onBook}
          className="
            w-full
            inline-flex items-center justify-center gap-2
            px-6 py-3 rounded-xl
            bg-rose-700 hover:bg-rose-800
            disabled:bg-stone-300
            text-white font-semibold text-sm
            transition-all duration-200
            disabled:cursor-not-allowed
            cursor-pointer
            shadow-lg shadow-rose-900/20
            hover:shadow-xl hover:shadow-rose-900/30
            disabled:shadow-none
          "
        >
          {isSubmitting
            ? "Creando reserva…"
            : allUnavailable
              ? "Sin disponibilidad"
              : totalRooms === 0
                ? "Selecciona habitaciones"
                : "Continuar al pago"}
          <ArrowRight className="size-4" />
        </button>

        <p className="text-[10px] text-stone-400 text-center -mt-2">
          No incluye impuestos ni cargos adicionales
        </p>
      </div>
    </aside>
  );
}
