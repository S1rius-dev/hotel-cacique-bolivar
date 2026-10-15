import { CalendarDays } from "lucide-react";
import { getRoomImages } from "@/components/home/rooms.data";
import type { BookingRoomType } from "@/features/booking/types";

interface Selection {
  tipo: BookingRoomType;
  quantity: number;
}

interface Props {
  selections?: Selection[];
  checkIn?: string;
  checkOut?: string;
  nights?: number;
  totalPrice?: number;
  extraPersonCharge?: number;
  totalRooms?: number;
  checkoutMonto?: number | null;
}

function formatPrice(value: number) {
  return `$${value.toLocaleString("es-CO")}`;
}

function formatCapacity(room: BookingRoomType): string {
  if (room.capacidadMaxima > room.capacidadBase) {
    return `${room.capacidadBase}-${room.capacidadMaxima} personas`;
  }
  return `${room.capacidadMaxima} ${room.capacidadMaxima === 1 ? "persona" : "personas"}`;
}

export default function BookingSummaryCard({
  selections = [],
  checkIn,
  checkOut,
  nights = 0,
  totalPrice = 0,
  extraPersonCharge = 0,
  checkoutMonto,
}: Props) {
  const visible = selections.filter((s) => s.quantity > 0);
  const subtotalHabitacion = totalPrice;
  const totalEstadia = totalPrice + (extraPersonCharge ?? 0);

  // Anticipo = monto real de Wompi si ya se inició checkout, si no estimado = primera noche
  const estimatedAnticipo =
    nights > 0 ? Math.round(totalEstadia / nights) : totalEstadia;
  const anticipo =
    checkoutMonto != null && Number(checkoutMonto) > 0
      ? Number(checkoutMonto)
      : estimatedAnticipo;
  const saldoPendiente = Math.max(0, totalEstadia - anticipo);

  const hasMultiple = visible.length > 1;

  return (
    <aside className="w-full rounded-2xl border border-stone-200 bg-white shadow-sm overflow-hidden">
      <div className="px-5 sm:px-6 pt-5 pb-6">
        <h2 className="text-xs font-semibold tracking-[0.14em] text-stone-800 uppercase">
          Resumen de tu reserva
        </h2>

        {/* Habitaciones - compacto para varias */}
        {visible.length > 0 ? (
          <div className={`mt-4 ${hasMultiple ? "space-y-2.5 max-h-[320px] overflow-y-auto pr-1 thin-scrollbar" : "space-y-3"}`}>
            {visible.map((sel) => {
              const images = getRoomImages(sel.tipo.nombre);
              const img = images?.[0];
              const eff = sel.tipo.precioNocheEfectivo ?? sel.tipo.precioBase;
              const priceTotal = eff * sel.quantity * (nights || 1);
              const pricePerNight = eff;
              return (
                <div
                  key={sel.tipo.id}
                  className="flex gap-3 rounded-xl border border-stone-200 bg-stone-50 p-3"
                >
                  {/* Thumb */}
                  {img ? (
                    <img
                      src={img}
                      alt={sel.tipo.nombre}
                      className="size-16 sm:size-[68px] rounded-lg object-cover shrink-0 bg-stone-100"
                    />
                  ) : (
                    <div className="size-16 sm:size-[68px] rounded-lg bg-stone-200 shrink-0" />
                  )}
                  <div className="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                    <div>
                      <div className="flex items-start justify-between gap-2">
                        <p className="text-sm font-semibold text-stone-800 leading-tight truncate">
                          {sel.tipo.nombre}
                        </p>
                        <span className="text-xs font-semibold bg-white border border-stone-200 rounded-full px-2 py-0.5 text-stone-600 shrink-0">
                          x{sel.quantity}
                        </span>
                      </div>
                      <p className="text-xs text-stone-500 mt-0.5 truncate">
                        {formatCapacity(sel.tipo)} · {formatPrice(pricePerNight)}/noche
                      </p>
                    </div>
                    <div className="flex items-center justify-between mt-1.5">
                      <span className="text-[11px] text-stone-400">
                        {nights} {nights === 1 ? "noche" : "noches"} {sel.quantity > 1 ? `· ${sel.quantity} hab.` : ""}
                      </span>
                      <span className="text-sm font-semibold text-stone-800 tabular-nums">
                        {formatPrice(priceTotal)}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="mt-4 rounded-xl border border-dashed border-stone-200 bg-stone-50 p-4">
            <p className="text-xs text-stone-500 text-center">Sin detalle de habitaciones</p>
          </div>
        )}

        {/* Fechas */}
        {checkIn && checkOut ? (
          <div className="mt-4 flex items-center justify-between rounded-xl border border-stone-200 bg-stone-50 px-3.5 py-3">
            <div className="flex items-center gap-2 min-w-0">
              <CalendarDays className="size-4 text-rose-500 shrink-0" />
              <span className="text-sm font-medium text-stone-800 truncate">
                {checkIn} → {checkOut}
              </span>
            </div>
            <span className="text-xs text-stone-500 shrink-0 ml-2">
              {nights} {nights === 1 ? "noche" : "noches"}
            </span>
          </div>
        ) : null}

        {/* Desglose */}
        <div className="mt-4 rounded-xl border border-stone-200 bg-stone-50 p-4 space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-stone-500 text-xs">Subtotal habitación{hasMultiple ? ` · ${visible.reduce((a, s) => a + s.quantity, 0)} hab.` : ""}</span>
            <span className="text-stone-700 font-medium tabular-nums text-xs">{formatPrice(subtotalHabitacion)}</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-stone-500 text-xs">Personas extra</span>
            <span className="text-stone-500 tabular-nums text-xs">
              {extraPersonCharge != null && extraPersonCharge > 0 ? formatPrice(extraPersonCharge) : "$0"}
            </span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-stone-500 text-xs">Servicios adicionales</span>
            <span className="text-stone-500 tabular-nums text-xs">$0</span>
          </div>
          <div aria-hidden className="h-px bg-stone-200 my-2" />
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold tracking-[0.12em] uppercase text-stone-700">
              Total estadía {nights ? `· ${nights} ${nights === 1 ? "noche" : "noches"}` : ""}
            </span>
            <span className="text-sm font-bold text-stone-900 tabular-nums">
              {formatPrice(totalEstadia)} COP
            </span>
          </div>
        </div>

        {/* Anticipo - política 1ª noche online: ocultar si es 1 sola noche (pago total en línea) */}
        {nights !== 1 ? (
          <div className="mt-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3.5 space-y-2">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold tracking-[0.12em] uppercase text-rose-800">A pagar ahora en línea</p>
                <p className="text-[11px] text-rose-700/70 mt-0.5">Anticipo · 1ª noche</p>
              </div>
              <span className="text-base font-bold text-rose-700 tabular-nums">
                {formatPrice(anticipo)} COP
              </span>
            </div>
            {saldoPendiente > 0 ? (
              <div className="flex items-center justify-between pt-2 border-t border-rose-200/60">
                <span className="text-xs text-rose-700/70">Saldo pendiente en hotel</span>
                <span className="text-xs font-medium text-stone-600 tabular-nums">
                  {formatPrice(saldoPendiente)} COP
                </span>
              </div>
            ) : null}
            <p className="text-[11px] text-rose-700/60 leading-relaxed">
              Solo pagas la primera noche en línea. El resto lo cancelas directamente en el hotel durante tu estadía.
            </p>
          </div>
        ) : null}

        {/* Cancelación */}
        <div className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50/60 px-3.5 py-3 flex gap-2.5">
          <span className="mt-0.5 text-emerald-600 text-sm leading-none">♢</span>
          <div>
            <p className="text-xs font-semibold text-emerald-900">Cancelación gratuita</p>
            <p className="text-xs text-emerald-700/80 leading-relaxed mt-0.5">
              Puedes cancelar sin costo hasta 24 h antes del check-in. Consulta nuestras políticas de cancelación.
            </p>
          </div>
        </div>
      </div>
    </aside>
  );
}
