import { CheckCircle2, Clock, XCircle } from "lucide-react";

type Estado = "PENDIENTE" | "PENDIENTE_PAGO" | "CONFIRMADA" | "EN_CURSO" | "FINALIZADA" | "CANCELADA" | "NO_SHOW";

const steps = [
  { key: "PENDIENTE", label: "Pendiente" },
  { key: "CONFIRMADA", label: "Confirmada" },
  { key: "EN_CURSO", label: "En curso" },
  { key: "FINALIZADA", label: "Finalizada" },
] as const;

interface Props {
  estado: Estado;
}

const estadoStyles: Record<Estado, { label: string; className: string; dot: React.ReactNode }> = {
  PENDIENTE: {
    label: "Pendiente",
    className: "bg-amber-50 text-amber-700 border-amber-200",
    dot: <Clock className="size-3.5" />,
  },
  PENDIENTE_PAGO: {
    label: "Pendiente de pago",
    className: "bg-amber-50 text-amber-700 border-amber-200",
    dot: <Clock className="size-3.5" />,
  },
  CONFIRMADA: {
    label: "Confirmada",
    className: "bg-blue-50 text-blue-700 border-blue-200",
    dot: <CheckCircle2 className="size-3.5" />,
  },
  EN_CURSO: {
    label: "En curso",
    className: "bg-emerald-50 text-emerald-700 border-emerald-200",
    dot: <CheckCircle2 className="size-3.5" />,
  },
  FINALIZADA: {
    label: "Finalizada",
    className: "bg-stone-100 text-stone-600 border-stone-200",
    dot: <CheckCircle2 className="size-3.5" />,
  },
  CANCELADA: {
    label: "Cancelada",
    className: "bg-red-50 text-red-700 border-red-200",
    dot: <XCircle className="size-3.5" />,
  },
  NO_SHOW: {
    label: "No llegó",
    className: "bg-red-50 text-red-700 border-red-200",
    dot: <XCircle className="size-3.5" />,
  },
};

export default function TimelineEstado({ estado }: Props) {
  const currentStyle = estadoStyles[estado] ?? estadoStyles.PENDIENTE;

  return (
    <div className="flex flex-col gap-1">
      <span
        className={`inline-flex items-center gap-1.5 self-start rounded-full border px-2.5 py-0.5 text-xs font-medium ${currentStyle.className}`}
      >
        {currentStyle.dot}
        {currentStyle.label}
      </span>

      {estado !== "CANCELADA" && estado !== "NO_SHOW" && (
        <div className="mt-2 flex items-center gap-1">
          {steps.map((step, i) => {
            const timelineKey = estado === "PENDIENTE_PAGO" ? "PENDIENTE" : estado;
            const currentIdx = steps.findIndex((s) => s.key === timelineKey);
            const completed = i <= currentIdx;
            const active = i === currentIdx;
            return (
              <div key={step.key} className="flex items-center gap-1">
                <div
                  className={`size-2 rounded-full transition-colors ${
                    active
                      ? "bg-rose-500 ring-2 ring-rose-200"
                      : completed
                        ? "bg-rose-400"
                        : "bg-stone-200"
                  }`}
                />
                {i < steps.length - 1 && (
                  <div
                    className={`h-px w-6 transition-colors ${
                      completed ? "bg-rose-300" : "bg-stone-200"
                    }`}
                  />
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
