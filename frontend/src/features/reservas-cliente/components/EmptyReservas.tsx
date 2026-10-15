import { CalendarX2 } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function EmptyReservas() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center gap-4 py-12 text-center">
      <div className="flex size-16 items-center justify-center rounded-full bg-rose-50">
        <CalendarX2 className="size-8 text-rose-400" />
      </div>
      <div>
        <h3 className="font-heading text-lg font-semibold text-stone-800">
          No tienes reservas
        </h3>
        <p className="mt-1 text-sm text-stone-400">
          Aún no has realizado ninguna reserva. ¡Empieza a planear tu estadía!
        </p>
      </div>
      <button
        type="button"
        onClick={() => navigate("/reservar")}
        className="mt-2 inline-flex items-center gap-2 rounded-xl bg-rose-800 px-6 py-2.5 text-sm font-medium text-white transition hover:bg-rose-900 cursor-pointer"
      >
        Reservar ahora
      </button>
    </div>
  );
}
