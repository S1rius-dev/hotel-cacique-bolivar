import { CheckCircle2, Circle, Loader2 } from "lucide-react";

interface TimelineStep {
  estado: string;
  label: string;
  descripcion: string;
  completado: boolean;
  activo: boolean;
}

interface PQRSTimelineProps {
  steps: TimelineStep[];
}

export default function PQRSTimeline({ steps }: PQRSTimelineProps) {
  return (
    <div className="relative">
      {steps.map((step, idx) => {
        const isLast = idx === steps.length - 1;

        return (
          <div key={step.estado} className="relative flex gap-4 pb-8 last:pb-0">
            {!isLast && (
              <div
                className={`absolute left-[15px] top-8 w-0.5 h-full -translate-x-1/2 ${
                  step.completado ? "bg-emerald-400" : "bg-stone-200"
                }`}
              />
            )}

            <div className="relative z-10 flex shrink-0">
              {step.completado ? (
                <CheckCircle2 className="size-7 text-emerald-500" />
              ) : step.activo ? (
                <span className="flex size-7 items-center justify-center rounded-full bg-rose-100">
                  <Loader2 className="size-4 animate-spin text-rose-600" />
                </span>
              ) : (
                <Circle className="size-7 text-stone-300" />
              )}
            </div>

            <div className="min-w-0 pt-0.5">
              <p
                className={`text-sm font-medium ${
                  step.activo
                    ? "text-rose-700"
                    : step.completado
                      ? "text-stone-800"
                      : "text-stone-400"
                }`}
              >
                {step.label}
              </p>
              <p className="text-xs text-stone-500">{step.descripcion}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
