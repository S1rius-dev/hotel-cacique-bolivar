import type { PagoEvento } from "../../types/dashboard.types";

function formatHora(iso: string) {
  const d = new Date(iso);
  return d.toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit", hour12: false });
}

interface Props {
  eventos: PagoEvento[];
}

export default function PagoTimeline({ eventos }: Props) {
  if (!eventos.length) {
    return <p className="text-xs text-muted-foreground italic">Sin trazabilidad aún</p>;
  }
  const isAprobada = (s: string) => s.toLowerCase().includes("aprobada");
  const isRechazada = (s: string) => s.toLowerCase().includes("rechazada");
  const isExpirada = (s: string) => s.toLowerCase().includes("expirada") || s.toLowerCase().includes("expiró");
  return (
    <div className="relative pl-2">
      <div className="space-y-0">
        {eventos.map((ev, idx) => {
          const isLast = idx === eventos.length - 1;
          const isFirst = idx === 0;
          const desc = String(ev.descripcion);
          const aprobada = isAprobada(desc);
          const rechazada = isRechazada(desc);
          const expirada = isExpirada(desc);
          // Color del dot según estado final
          const dotClass = aprobada
            ? "bg-[#10b981] border-[#10b981]"
            : rechazada
              ? "bg-[#ef4444] border-[#ef4444]"
              : expirada
                ? "bg-zinc-400 border-zinc-400"
                : isFirst
                  ? "bg-[#c81e3a] border-[#c81e3a]"
                  : isLast
                    ? "bg-card border-muted-foreground"
                    : "bg-card border-border";
          const innerClass = aprobada || rechazada || expirada || isFirst ? "bg-white" : "bg-muted-foreground";
          const lineClass = aprobada ? "bg-[#10b981]/30" : rechazada ? "bg-[#ef4444]/30" : "bg-border";
          return (
            <div key={ev.id} className="relative flex gap-3">
              {/* line */}
              {!isLast && (
                <div className={`absolute left-[7px] top-[14px] bottom-[-8px] w-px ${lineClass}`} aria-hidden />
              )}
              {/* dot */}
              <div className={`mt-1 size-[14px] rounded-full border-2 shrink-0 flex items-center justify-center ${dotClass}`}>
                <span className={`size-1.5 rounded-full ${innerClass}`} />
              </div>
              <div className="pb-4 flex-1 min-w-0">
                <div className="flex items-baseline justify-between gap-2">
                  <p className={`text-sm font-medium ${aprobada ? "text-[#10b981]" : rechazada ? "text-[#ef4444]" : "text-foreground"}`}>{ev.descripcion}</p>
                  <span className="text-xs font-mono text-muted-foreground shrink-0">{formatHora(ev.timestamp)}</span>
                </div>
                {ev.subtitulo && <p className="text-xs text-muted-foreground mt-0.5">{ev.subtitulo}</p>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
