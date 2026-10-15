import { CalendarCheck } from "lucide-react";

interface Props {
  codigoReserva?: string;
  estado?: string;
}

const estadoStyles: Record<string, { label: string; className: string }> = {
  PENDIENTE: { label: "Pendiente", className: "bg-yellow-100 text-yellow-700 border-yellow-200" },
  CONFIRMADA: { label: "Confirmada", className: "bg-blue-100 text-blue-700 border-blue-200" },
  EN_CURSO: { label: "En curso", className: "bg-green-100 text-green-700 border-green-200" },
  FINALIZADA: { label: "Finalizada", className: "bg-zinc-100 text-zinc-700 border-zinc-200" },
  CANCELADA: { label: "Cancelada", className: "bg-red-100 text-red-700 border-red-200" },
  NO_SHOW: { label: "No Llegó", className: "bg-gray-100 text-gray-700 border-gray-200" },
};

function ReservaDetalleInfoHeader({ codigoReserva, estado }: Props) {
  const badge = estado ? estadoStyles[estado] : null;

  return (
    <div className="flex items-start gap-3 min-w-0">
      <div className="w-10 h-10 rounded-full bg-zinc-100 flex items-center justify-center border mt-1 shrink-0">
        <CalendarCheck className="w-5 h-5 text-zinc-500" />
      </div>

      <div className="flex flex-col min-w-0">
        <h1 className="text-xl sm:text-2xl font-semibold tracking-tight leading-tight truncate">
          Reserva {codigoReserva ?? "—"}
        </h1>

        {badge && (
          <div className="mt-1">
            <span
              className={`inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full border font-medium ${badge.className}`}
            >
              {badge.label}
            </span>
          </div>
        )}
      </div>
    </div>
  );
}

export default ReservaDetalleInfoHeader;
