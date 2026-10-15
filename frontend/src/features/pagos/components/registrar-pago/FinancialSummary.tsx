import { FORMAT_CURRENCY } from "@/features/pagos";
import type { AjusteResponse, PricingBreakdown } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  desglose?: PricingBreakdown;
  subtotal: number;
  sumaAjustes: number;
  total: number;
  totalPagado: number;
  saldoPendiente: number;
  ajustes?: AjusteResponse[];
}

function FinancialSummary({
  desglose,
  subtotal,
  sumaAjustes,
  total,
  totalPagado,
  saldoPendiente,
  ajustes,
}: Props) {
  const pending = saldoPendiente;
  // El unico dato que se lee de `ajustes` es la existencia de un total fijado,
  // para etiquetarlo. Los importes por linea los trae el backend ya resueltos en
  // `desglose.lineas[].impacto`.
  const lastTotal = [...(ajustes ?? [])].reverse().find((a) => a.modoCalculo === "TOTAL" && a.activo !== false);
  const lineas = (desglose?.lineas ?? []).filter((l) => l.tipoAjuste !== "CORTESIA");
  const hayDetalle =
    !!lastTotal ||
    lineas.length > 0 ||
    (!lastTotal && !desglose && sumaAjustes !== 0);

  return (
    <div className="flex flex-col gap-3 rounded-lg bg-zinc-50/80 dark:bg-zinc-900/60 backdrop-blur-sm shadow-sm border border-zinc-200/60 dark:border-zinc-800/60 p-4">
      {hayDetalle && (
        <div className="space-y-1.5">
          {lastTotal ? (
            <div className="flex justify-between gap-2 text-[13px]">
              <span className="text-emerald-700 dark:text-emerald-400 truncate flex items-center gap-1">
                {lastTotal.concepto ?? "Total fijado manualmente"}
                <span className="text-[11px] opacity-60">(fijo)</span>
              </span>
              <span className="font-medium tabular-nums text-emerald-700 shrink-0">
                {FORMAT_CURRENCY.format(lastTotal.valor ?? 0)}
              </span>
            </div>
          ) : null}
          {lineas.length > 0 ? (
            lineas.map((l, i) => {
              const impacto = l.impacto ?? 0;
              const esDescuento = impacto < 0;
              return (
                <div key={l.ajusteId ?? i} className="flex justify-between gap-2 text-[13px] pl-2">
                  <span className="text-muted-foreground truncate flex items-center gap-1">
                    {l.concepto ?? (esDescuento ? "Descuento" : "Cargo")}
                    {l.modoCalculo === "PORCENTUAL" && (
                      <span className="text-[11px] text-muted-foreground/60">({l.valor}%)</span>
                    )}
                  </span>
                  <span
                    className={`font-medium tabular-nums shrink-0 ${
                      esDescuento ? "text-green-600" : "text-amber-600"
                    }`}
                  >
                    {impacto > 0 ? "+" : ""}
                    {FORMAT_CURRENCY.format(impacto)}
                  </span>
                </div>
              );
            })
          ) : !lastTotal && !desglose && sumaAjustes !== 0 ? (
            <div className="flex justify-between gap-2 text-[13px]">
              <span className="text-muted-foreground">Ajustes</span>
              <span className={`font-medium tabular-nums shrink-0 ${sumaAjustes >= 0 ? "text-amber-600" : "text-green-600"}`}>
                {sumaAjustes >= 0 ? "+" : ""}{FORMAT_CURRENCY.format(sumaAjustes)}
              </span>
            </div>
          ) : null}
        </div>
      )}

      <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-4">
        <div className="min-w-0 rounded-md border border-zinc-200/60 dark:border-zinc-800/60 bg-white/60 dark:bg-zinc-800/40 px-3 py-2">
          <p className="text-[10px] uppercase tracking-wide text-muted-foreground/70">Subtotal</p>
          <p className="text-sm font-semibold tabular-nums truncate">{FORMAT_CURRENCY.format(subtotal)}</p>
        </div>
        <div className="min-w-0 rounded-md border border-zinc-200/60 dark:border-zinc-800/60 bg-white/60 dark:bg-zinc-800/40 px-3 py-2">
          <p className="text-[10px] uppercase tracking-wide text-muted-foreground/70">
            Total{lastTotal ? " (fijado)" : ""}
          </p>
          <p className="text-sm font-semibold tabular-nums truncate">{FORMAT_CURRENCY.format(total)}</p>
          {lastTotal && subtotal !== total && (
            <p className="text-[11px] text-muted-foreground/70 tabular-nums truncate">
              antes {FORMAT_CURRENCY.format(subtotal)}
            </p>
          )}
        </div>
        <div className="min-w-0 rounded-md border border-zinc-200/60 dark:border-zinc-800/60 bg-white/60 dark:bg-zinc-800/40 px-3 py-2">
          <p className="text-[10px] uppercase tracking-wide text-muted-foreground/70">Pagado</p>
          <p className="text-sm font-semibold tabular-nums truncate text-emerald-600">
            {FORMAT_CURRENCY.format(totalPagado)}
          </p>
        </div>
        <div className="min-w-0 rounded-md border border-zinc-200/60 dark:border-zinc-800/60 bg-white/60 dark:bg-zinc-800/40 px-3 py-2">
          <p className="text-[10px] uppercase tracking-wide text-muted-foreground/70">Pendiente</p>
          <p
            className={`text-sm font-semibold tabular-nums truncate ${pending > 0 ? "text-amber-600" : "text-zinc-400"}`}
          >
            {pending > 0 ? FORMAT_CURRENCY.format(pending) : "\u2014"}
          </p>
        </div>
      </div>

      {total > 0 && (
        <div className="w-full h-1.5 rounded-full bg-zinc-200/70 dark:bg-zinc-700/70 overflow-hidden">
          <div
            className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-emerald-400 transition-all duration-500"
            style={{ width: `${Math.min(100, Math.round((totalPagado / total) * 100))}%` }}
          />
        </div>
      )}
    </div>
  );
}

export default FinancialSummary;
