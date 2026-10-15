import { Building2, Store, Wallet } from "lucide-react";
import { FORMAT_CURRENCY, getOrigenCobroStyle } from "@/features/pagos/constants/pago.constants";

/**
 * Forma mínima de una línea de desglose. La comparten la vista previa
 * (`PagoDesgloseLineaResponse`) y el desglose ya registrado
 * (`PagoDesgloseResponse`), que solo difieren en el `id`.
 */
interface LineaDesglose {
  origen?: string;
  monto?: number;
  concepto?: string;
  consumoId?: number;
}

interface Props {
  lineas: LineaDesglose[] | undefined;
  isLoading?: boolean;
  titulo?: string;
}

const ORIGEN_ICON: Record<string, typeof Building2> = {
  RECEPCION: Building2,
  TIENDA: Store,
};

interface Grupo {
  origen: string;
  total: number;
  lineas: LineaDesglose[];
}

/** Agrupa por origen conservando el orden de llegada y suma cada grupo. */
function agruparPorOrigen(lineas: LineaDesglose[]): Grupo[] {
  const grupos = new Map<string, Grupo>();
  for (const linea of lineas) {
    const origen = String(linea.origen ?? "").toUpperCase();
    const monto = linea.monto ?? 0;
    const grupo = grupos.get(origen);
    if (grupo) {
      grupo.total += monto;
      grupo.lineas.push(linea);
    } else {
      grupos.set(origen, { origen, total: monto, lineas: [linea] });
    }
  }
  return [...grupos.values()];
}

function DesgloseSkeleton() {
  return (
    <div className="rounded-xl border border-zinc-200/60 dark:border-zinc-800/60 bg-zinc-50/80 dark:bg-zinc-900/40 p-3.5 animate-pulse">
      <div className="h-2.5 w-32 rounded bg-zinc-200 dark:bg-zinc-800 mb-3" />
      <div className="h-8 rounded-lg bg-zinc-200/70 dark:bg-zinc-800/70" />
    </div>
  );
}

/**
 * Muestra el reparto de un pago entre las cajas. Recepción se cubre primero y
 * con el remanente se pagan los consumos de tienda, así que el bloque de tienda
 * solo aparece si el monto alcanza para cubrir algo de ella.
 */
export default function DesglosePreview({ lineas, isLoading, titulo }: Props) {
  if (isLoading) return <DesgloseSkeleton />;

  const grupos = agruparPorOrigen(lineas ?? []);
  if (grupos.length === 0) return null;

  return (
    <div className="rounded-xl border border-zinc-200/60 dark:border-zinc-800/60 bg-zinc-50/80 dark:bg-zinc-900/40 p-4 space-y-3">
      <p className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground/70">
        {titulo ?? "Reparto entre cajas"}
      </p>

      {grupos.map((grupo) => {
        const { label, className } = getOrigenCobroStyle(grupo.origen);
        const Icon = ORIGEN_ICON[grupo.origen] ?? Wallet;
        return (
          <div
            key={grupo.origen}
            className="rounded-lg border border-zinc-200/60 dark:border-zinc-800/60 bg-white/60 dark:bg-zinc-900/40"
          >
            <div className="flex items-center justify-between gap-2 px-3 py-2.5">
              <span
                className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full border ${className}`}
              >
                <Icon className="size-3.5" />
                {label}
              </span>
              <span className="text-sm font-bold tabular-nums text-zinc-800 dark:text-zinc-200">
                {FORMAT_CURRENCY.format(grupo.total)}
              </span>
            </div>

            {grupo.lineas.length > 1 && (
              <div className="border-t border-zinc-200/60 dark:border-zinc-800/60 px-3 py-2 space-y-1.5">
                {grupo.lineas.map((linea, idx) => (
                  <div
                    key={linea.consumoId ?? idx}
                    className="flex items-center justify-between gap-2 text-xs"
                  >
                    <span className="text-muted-foreground truncate">{linea.concepto ?? "—"}</span>
                    <span className="font-medium tabular-nums text-zinc-600 dark:text-zinc-300 shrink-0">
                      {FORMAT_CURRENCY.format(linea.monto ?? 0)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
