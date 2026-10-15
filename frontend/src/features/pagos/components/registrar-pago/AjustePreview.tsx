import { FORMAT_CURRENCY } from "@/features/pagos";

interface Props {
  impact: number;
  nuevoTotal: number;
  nuevoPendiente: number;
}

function AjustePreview({ impact, nuevoTotal, nuevoPendiente }: Props) {
  return (
    <div className="rounded-xl bg-gradient-to-br from-emerald-500/10 to-emerald-500/5 dark:from-emerald-500/15 dark:to-emerald-500/5 border border-emerald-200/40 dark:border-emerald-500/20 p-4 space-y-2.5 shadow-sm">
      <p className="text-[10px] font-semibold uppercase tracking-widest text-emerald-600/70 dark:text-emerald-400/70">
        Vista previa del impacto
      </p>
      <div className="flex items-baseline gap-1.5">
        <span className="text-sm text-muted-foreground">Impacto</span>
        <span className={`text-2xl font-bold tabular-nums ${impact >= 0 ? "text-amber-500" : "text-green-500"}`}>
          {impact > 0 ? "+" : ""}{FORMAT_CURRENCY.format(Math.abs(impact))}
        </span>
      </div>
      <div className="h-px bg-gradient-to-r from-emerald-500/20 via-emerald-500/5 to-transparent" />
      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-0.5">
          <p className="text-[10px] text-muted-foreground/70 uppercase tracking-wider">Nuevo total</p>
          <p className="text-base font-semibold tabular-nums text-zinc-900 dark:text-zinc-100">{FORMAT_CURRENCY.format(nuevoTotal)}</p>
        </div>
        <div className="space-y-0.5">
          <p className="text-[10px] text-muted-foreground/70 uppercase tracking-wider">Saldo pendiente</p>
          <p className="text-base font-semibold tabular-nums text-amber-600 dark:text-amber-400">{FORMAT_CURRENCY.format(nuevoPendiente)}</p>
        </div>
      </div>
    </div>
  );
}

export default AjustePreview;
