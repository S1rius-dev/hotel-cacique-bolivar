import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { formatCurrency } from "@/utils/format";
import type { IngresosResponse, OcupacionResponse } from "@/api/generated/openAPIDefinition.schemas";

type Rango = "HOY" | "SEMANA" | "MES";

const RANGOS: { key: Rango; label: string }[] = [
  { key: "HOY", label: "Hoy" },
  { key: "SEMANA", label: "Semana" },
  { key: "MES", label: "Mes" },
];

const formatEjeX = (fecha: string, rango: Rango): string => {
  if (rango === "HOY") {
    const h = fecha.match(/T(\d{2})/)?.[1];
    return h ? `${h}:00` : fecha;
  }
  const [, m, d] = fecha.split("-");
  return `${d}/${m}`;
};

const FORMAT_MONEDA = (v: number) => formatCurrency(v);

interface Props {
  ingresos?: IngresosResponse;
  ocupacion?: OcupacionResponse;
  isLoading?: boolean;
  rangoIngresos: Rango;
  rangoOcupacion: Rango;
  onRangoIngresosChange: (r: Rango) => void;
  onRangoOcupacionChange: (r: Rango) => void;
}

export const ChartsSection = ({
  ingresos,
  ocupacion,
  isLoading,
  rangoIngresos,
  rangoOcupacion,
  onRangoIngresosChange,
  onRangoOcupacionChange,
}: Props) => {
  const ingresosData = (ingresos?.data ?? []).map((d) => ({
    fecha: d.fecha ?? "",
    label: formatEjeX(d.fecha ?? "", rangoIngresos),
    total: d.total ?? 0,
  }));

  const ocupacionData = (() => {
    const raw = ocupacion?.data ?? [];
    if (raw.length === 0) return [];

    if (rangoOcupacion === "HOY") {
      const bins: { label: string; values: number[] }[] = [];
      for (let i = 0; i < 24; i += 4) {
        const from = `${String(i).padStart(2, "0")}:00`;
        bins.push({ label: `${from}`, values: [] });
      }
      raw.forEach((d) => {
        const h = parseInt(d.fecha?.match(/T(\d{2})/)?.[1] ?? "0", 10);
        const idx = Math.min(Math.floor(h / 4), 5);
        bins[idx].values.push(d.porcentaje ?? 0);
      });
      return bins.map((b) => ({
        fecha: b.label,
        label: b.label,
        porcentaje: b.values.length > 0
          ? Math.round(b.values.reduce((a, v) => a + v, 0) / b.values.length)
          : 0,
        ocupadas: 0,
      }));
    }

    if (rangoOcupacion === "MES") {
      const bins: { label: string; values: number[] }[] = [];
      for (let i = 0; i < 6; i++) {
        bins.push({ label: `${i * 5 + 1}-${Math.min((i + 1) * 5, 30)}`, values: [] });
      }
      raw.forEach((d) => {
        const day = parseInt(d.fecha?.split("-")[2] ?? "1", 10);
        const idx = Math.min(Math.floor((day - 1) / 5), 5);
        bins[idx].values.push(d.porcentaje ?? 0);
      });
      return bins.map((b) => ({
        fecha: b.label,
        label: b.label,
        porcentaje: b.values.length > 0
          ? Math.round(b.values.reduce((a, v) => a + v, 0) / b.values.length)
          : 0,
        ocupadas: 0,
      }));
    }

    return raw.map((d) => ({
      fecha: d.fecha ?? "",
      label: formatEjeX(d.fecha ?? "", rangoOcupacion),
      porcentaje: d.porcentaje ?? 0,
      ocupadas: d.ocupadas ?? 0,
    }));
  })();

  return (
    <div className="grid gap-4 lg:grid-cols-2">
      {/* Ingresos — AreaChart */}
      <Card className="rounded-2xl shadow-sm hover:shadow-md transition-all">
        <CardHeader className="flex flex-row items-center justify-between flex-wrap gap-2">
          <CardTitle className="text-base font-semibold">Ingresos</CardTitle>
          <div className="flex gap-1">
            {RANGOS.map((r) => (
              <Button
                key={r.key}
                variant={rangoIngresos === r.key ? "default" : "ghost"}
                size="sm"
                onClick={() => onRangoIngresosChange(r.key)}
                className={`text-xs h-7 px-2.5 ${
                  rangoIngresos === r.key
                    ? "bg-rose-800 hover:bg-rose-900 text-white"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                {r.label}
              </Button>
            ))}
          </div>
        </CardHeader>
        <CardContent className="h-[220px] lg:h-[260px]">
          {isLoading ? (
            <div className="h-full flex items-center justify-center">
              <Loader2 className="size-6 animate-spin text-muted-foreground" />
            </div>
          ) : ingresosData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={ingresosData} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="ingresosGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#be123c" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#be123c" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis
                  dataKey="label"
                  tick={{ fontSize: 10 }}
                  tickLine={false}
                  axisLine={false}
                  interval="preserveStartEnd"
                />
                <YAxis
                  tick={{ fontSize: 10 }}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(v: number) => `$${(v / 1000).toFixed(0)}k`}
                  width={40}
                />
                  <Tooltip
                    formatter={(value) => [FORMAT_MONEDA(Number(value)), "Ingresos"]}
                  labelFormatter={() => ""}
                  contentStyle={{
                    borderRadius: 8,
                    border: "1px solid hsl(var(--border))",
                    boxShadow: "0 4px 12px rgba(0,0,0,0.08)",
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="total"
                  stroke="#be123c"
                  strokeWidth={2}
                  fill="url(#ingresosGrad)"
                  animationDuration={800}
                />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
              Sin datos
            </div>
          )}
        </CardContent>
      </Card>

      {/* Ocupación — barras horizontales */}
      <Card className="rounded-2xl shadow-sm hover:shadow-md transition-all">
        <CardHeader className="flex flex-row items-center justify-between flex-wrap gap-2">
          <CardTitle className="text-base font-semibold">Ocupación</CardTitle>
          <div className="flex gap-1">
            {RANGOS.map((r) => (
              <Button
                key={r.key}
                variant={rangoOcupacion === r.key ? "default" : "ghost"}
                size="sm"
                onClick={() => onRangoOcupacionChange(r.key)}
                className={`text-xs h-7 px-2.5 ${
                  rangoOcupacion === r.key
                    ? "bg-rose-800 hover:bg-rose-900 text-white"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                {r.label}
              </Button>
            ))}
          </div>
        </CardHeader>
        <CardContent className="h-[220px] lg:h-[260px]">
          {isLoading ? (
            <div className="h-full flex items-center justify-center">
              <Loader2 className="size-6 animate-spin text-muted-foreground" />
            </div>
          ) : ocupacionData.length > 0 ? (
            <div className="h-full flex flex-col justify-center gap-2 overflow-y-auto pr-1">
              {ocupacionData.map((d, i) => {
                const pct = d.porcentaje;
                const color =
                  pct >= 75
                    ? "bg-rose-500"
                    : pct >= 50
                      ? "bg-amber-500"
                      : "bg-emerald-500";

                return (
                  <div key={i} className="flex items-center gap-2">
                    <span className="text-[11px] text-muted-foreground w-14 shrink-0 text-right font-medium tabular-nums truncate">
                      {d.label}
                    </span>
                    <div className="flex-1 h-5 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${color} transition-all duration-700 ease-out`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <span className="text-xs font-semibold text-zinc-700 dark:text-zinc-300 w-10 shrink-0 tabular-nums">
                      {pct.toFixed(0)}%
                    </span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
              Sin datos
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
