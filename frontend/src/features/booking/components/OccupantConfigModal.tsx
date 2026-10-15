import { useState, useEffect, useMemo } from "react";
import { Users, UserPlus, Minus, Plus, X } from "lucide-react";
import type { BookingRoomType } from "@/features/booking/types";

interface Props {
  open: boolean;
  onClose: () => void;
  roomType: BookingRoomType;
  quantity: number;
  occupants: number[];
  onOccupantsChange: (occupants: number[]) => void;
  onConfirm: () => void;
}

function formatPrice(value: number) {
  return `$${value.toLocaleString("es-CO")}`;
}

export default function OccupantConfigModal({
  open,
  onClose,
  roomType,
  quantity,
  occupants,
  onOccupantsChange,
  onConfirm,
}: Props) {
  const [localOccupants, setLocalOccupants] = useState<number[]>([]);

  // La lista local arranca desde la confirmacion vigente (mismo largo que
  // `quantity`, reutilizando lo ya pagado). Se ajusta en render cuando cambian
  // los props, en lugar de en un efecto.
  const signature = `${open}|${quantity}|${occupants.join(",")}|${roomType.capacidadBase}`;
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    if (open) {
      setLocalOccupants(
        occupants.length === quantity
          ? occupants
          : Array.from(
              { length: quantity },
              (_, i) => occupants[i] ?? roomType.capacidadBase,
            ),
      );
    }
  }

  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
      return () => { document.body.style.overflow = ""; };
    }
  }, [open]);

  const totalGuests = useMemo(
    () => localOccupants.reduce((a, b) => a + b, 0),
    [localOccupants],
  );

  const totalExtras = useMemo(
    () => localOccupants.reduce((sum, occ) => sum + Math.max(0, occ - roomType.capacidadBase), 0),
    [localOccupants, roomType.capacidadBase],
  );

  const handleChange = (index: number, value: number) => {
    const next = [...localOccupants];
    next[index] = value;
    setLocalOccupants(next);
  };

  const capacidadLabel = roomType.capacidadBase === roomType.capacidadMaxima
    ? `Hasta ${roomType.capacidadBase} huéspedes`
    : `${roomType.capacidadBase} - ${roomType.capacidadMaxima} huéspedes`;

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="relative w-full max-w-lg rounded-3xl bg-white shadow-2xl overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="bg-gradient-to-r from-rose-800 to-rose-700 px-6 py-5">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-bold text-white">
                {roomType.nombre}
              </h3>
              <div className="flex items-center gap-1.5 mt-1">
                <Users className="size-3.5 text-rose-200" />
                <span className="text-xs text-rose-200">{capacidadLabel}</span>
              </div>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 rounded-full text-rose-200 hover:text-white hover:bg-white/10 transition-all cursor-pointer"
            >
              <X className="size-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="px-6 py-5 space-y-4 max-h-[50vh] overflow-y-auto">
          <p className="text-xs text-stone-500 font-medium uppercase tracking-wider">
            Configurar ocupantes por habitación
          </p>

          {localOccupants.map((occ, i) => {
            const extra = Math.max(0, occ - roomType.capacidadBase);
            const effBase = roomType.precioNocheEfectivo ?? roomType.precioBase;
            const effExtra = roomType.precioPersonaExtraEfectivo ?? roomType.precioPersonaExtra;
            const roomPrice = effBase + extra * effExtra;

            return (
              <div
                key={i}
                className="p-4 rounded-xl border border-stone-200 bg-stone-50/50"
              >
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm font-semibold text-stone-800">
                    Habitación {i + 1}
                  </span>
                  <span className="text-xs text-stone-400">
                    {formatPrice(roomPrice)} /noche
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-xs text-stone-500">Huéspedes</span>
                  <div className="inline-flex items-center gap-3">
                    <button
                      type="button"
                      onClick={() => handleChange(i, Math.max(roomType.capacidadBase, occ - 1))}
                      disabled={occ <= roomType.capacidadBase}
                      className="size-8 rounded-full flex items-center justify-center border border-stone-200 text-stone-500 transition-all duration-200 hover:border-rose-300 hover:text-rose-600 hover:bg-rose-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
                    >
                      <Minus className="size-3.5" />
                    </button>
                    <span className="min-w-[2ch] text-center text-lg font-semibold tabular-nums text-stone-800 leading-none">
                      {occ}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleChange(i, Math.min(roomType.capacidadMaxima, occ + 1))}
                      disabled={occ >= roomType.capacidadMaxima}
                      className="size-8 rounded-full flex items-center justify-center border border-stone-200 text-stone-500 transition-all duration-200 hover:border-rose-300 hover:text-rose-600 hover:bg-rose-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
                    >
                      <Plus className="size-3.5" />
                    </button>
                  </div>
                </div>

                {extra > 0 && (
                  <div className="flex items-center gap-1.5 mt-2 text-xs text-amber-600 bg-amber-50 px-2.5 py-1.5 rounded-lg w-fit">
                    <UserPlus className="size-3.5" />
                    <span>
                      {extra} extra{extra !== 1 ? "s" : ""} (+{formatPrice(extra * (roomType.precioPersonaExtraEfectivo ?? roomType.precioPersonaExtra))})
                    </span>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-stone-200 bg-stone-50">
          <div className="flex items-center justify-between mb-4">
            <span className="text-sm text-stone-600">
              Total huéspedes
            </span>
            <span className="text-lg font-bold text-stone-900 tabular-nums">
              {totalGuests}
            </span>
          </div>

          {totalExtras > 0 && (
            <div className="flex items-center justify-between mb-4 text-xs text-amber-600">
              <span>
                Personas extra ({totalExtras})
              </span>
              <span className="font-semibold">
                +{formatPrice(totalExtras * (roomType.precioPersonaExtraEfectivo ?? roomType.precioPersonaExtra))} /noche
              </span>
            </div>
          )}

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2.5 rounded-xl border border-stone-200 text-stone-600 font-semibold text-sm hover:bg-stone-100 transition-all cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={() => {
                onOccupantsChange(localOccupants);
                onConfirm();
              }}
              className="flex-1 px-4 py-2.5 rounded-xl bg-rose-700 hover:bg-rose-800 text-white font-semibold text-sm transition-all cursor-pointer shadow-lg shadow-rose-900/20"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
