import { ArrowUpRight, CreditCard, Clock, Building2, Store } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { PagosMetrics as Metrics } from "../../types/dashboard.types";

interface Props {
  metrics: Metrics;
  gatewayDisplayName?: string;
}

export default function PagosMetrics({ metrics, gatewayDisplayName = "Pasarela online" }: Props) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-5 gap-4">
      {/* Recaudado hoy */}
      <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">Recaudado hoy</span>
          <span className="size-8 rounded-lg bg-muted border border-border flex items-center justify-center">
            <ArrowUpRight className="size-4 text-muted-foreground" />
          </span>
        </div>
        <p className="text-2xl font-bold tracking-tight text-foreground">{FORMAT_CURRENCY.format(metrics.recaudadoHoy)}</p>
        <p className="text-xs text-muted-foreground">{metrics.wompi.count + metrics.efectivo.count} transacciones en total</p>
      </div>

      {/* Pasarela online */}
      <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">{gatewayDisplayName} (online)</span>
          <span className="size-8 rounded-lg bg-[#1e3a5f]/20 dark:bg-[#1e3a5f]/40 border border-[#2a4a7a]/30 flex items-center justify-center">
            <CreditCard className="size-4 text-[#3b82f6]" />
          </span>
        </div>
        <p className="text-2xl font-bold tracking-tight text-foreground">{FORMAT_CURRENCY.format(metrics.wompi.monto)}</p>
        <p className="text-xs text-muted-foreground">{metrics.wompi.count} transacciones en línea</p>
      </div>

      {/* Efectivo Recepción */}
      <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">Efectivo · Recepción</span>
          <span className="size-8 rounded-lg bg-[#1a3329]/20 dark:bg-[#1a3329]/60 border border-[#14532d]/30 flex items-center justify-center">
            <Building2 className="size-4 text-[#22c55e]" />
          </span>
        </div>
        <p className="text-2xl font-bold tracking-tight text-foreground">{FORMAT_CURRENCY.format(metrics.efectivoRecepcion.monto)}</p>
        <p className="text-xs text-muted-foreground">{metrics.efectivoRecepcion.count} registros · Caja recepción</p>
      </div>

      {/* Caja Tienda */}
      <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3 border-t-2 border-t-[#c81e3a]/20">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">Caja · Tienda</span>
          <span className="size-8 rounded-lg bg-[#c81e3a]/10 dark:bg-[#c81e3a]/20 border border-[#c81e3a]/20 flex items-center justify-center">
            <Store className="size-4 text-[#c81e3a]" />
          </span>
        </div>
        <p className="text-2xl font-bold tracking-tight text-foreground">{FORMAT_CURRENCY.format(metrics.efectivoTienda.monto)}</p>
        <p className="text-xs text-muted-foreground">{metrics.efectivoTienda.count} ventas hoy · Mostrador</p>
      </div>

      {/* Pendientes */}
      <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">Pendientes</span>
          <span className="size-8 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
            <Clock className="size-4 text-[#f59e0b]" />
          </span>
        </div>
        <p className="text-2xl font-bold tracking-tight text-[#f59e0b]">{metrics.pendientes}</p>
        <p className="text-xs text-muted-foreground">Requieren seguimiento</p>
      </div>
    </div>
  );
}
