import { CreditCard, Pencil, XCircle } from "lucide-react";
import type { AccionesEvaluadas, AccionEstado } from "./ReservaCard";

interface Props {
  acciones: AccionesEvaluadas;
  onCancel: () => void;
  onEdit: () => void;
  onPay?: () => void;
  isPast: boolean;
}

const tooltipText: Record<AccionEstado, string | null> = {
  activo: null,
  proximo: null,
  vencido: null,
  no_disponible: "No disponible para esta reserva",
};

export default function ReservaActions({ acciones, onCancel, onEdit, onPay, isPast }: Props) {
  if (isPast) {
    return (
      <div className="mt-4 pt-3 border-t border-stone-100">
        <p className="text-xs text-stone-400 italic">Esta reserva ya finalizó</p>
      </div>
    );
  }

  const cancelDisabled = acciones.cancelar !== "activo";
  const editDisabled = acciones.editar !== "activo";
  const cancelTooltip = tooltipText[acciones.cancelar];
  const editTooltip = tooltipText[acciones.editar];

  return (
    <div className="mt-4 flex flex-wrap items-center gap-2 pt-3 border-t border-stone-100">
      {onPay ? (
        <button
          type="button"
          onClick={onPay}
          className="inline-flex items-center gap-1.5 rounded-xl bg-rose-700 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-800 cursor-pointer shadow-sm shadow-rose-900/20"
        >
          <CreditCard className="size-3.5" />
          Pagar
        </button>
      ) : null}

      <div className="relative group">
        <button
          type="button"
          onClick={onCancel}
          disabled={cancelDisabled}
          className="inline-flex items-center gap-1.5 rounded-xl border border-red-200 px-3.5 py-1.5 text-xs font-medium text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent cursor-pointer"
        >
          <XCircle className="size-3.5" />
          Cancelar
        </button>
        {cancelDisabled && cancelTooltip && (
          <div className="absolute bottom-full left-0 mb-2 hidden w-56 rounded-lg border border-stone-200 bg-white p-2 text-xs text-stone-500 shadow-lg group-hover:block">
            {cancelTooltip}
          </div>
        )}
      </div>

      <div className="relative group">
        <button
          type="button"
          onClick={onEdit}
          disabled={editDisabled}
          className="inline-flex items-center gap-1.5 rounded-xl border border-stone-200 px-3.5 py-1.5 text-xs font-medium text-stone-600 transition hover:bg-stone-50 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent cursor-pointer"
        >
          <Pencil className="size-3.5" />
          Editar fechas
        </button>
        {editDisabled && editTooltip && (
          <div className="absolute bottom-full left-0 mb-2 hidden w-56 rounded-lg border border-stone-200 bg-white p-2 text-xs text-stone-500 shadow-lg group-hover:block">
            {editTooltip}
          </div>
        )}
      </div>
    </div>
  );
}
