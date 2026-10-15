import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Wallet,
  Loader2,
  DollarSign,
  ArrowUpRight,
  ArrowDownRight,
  AlertTriangle,
  ArrowRight,
} from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { useCerrarTurno } from "../hooks/useTurnoMutations";
import type { TurnoResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { CajaNombre } from "../constants/turno.constants";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  turno: TurnoResponse;
  caja?: CajaNombre;
}

export default function CerrarTurnoModal({
  open,
  onOpenChange,
  turno,
  caja = "RECEPCION",
}: Props) {
  const [contadoFinal, setContadoFinal] = useState("");
  const [observaciones, setObservaciones] = useState("");
  const cerrarMutation = useCerrarTurno({ caja }, () => {
    onOpenChange(false);
    setContadoFinal("");
    setObservaciones("");
  });

  const fondo = turno.fondoInicial ?? 0;
  const ingresos = turno.totalIngresosEfectivo ?? 0;
  const egresos = turno.totalEgresos ?? 0;
  const esperado = turno.esperadoCierre ?? 0;

  const contadoNum = Number(contadoFinal.replace(/[^0-9]/g, ""));
  const diferencia = contadoNum > 0 ? contadoNum - esperado : 0;
  const hayDiferencia = contadoNum > 0 && diferencia !== 0;

  const handleSubmit = () => {
    cerrarMutation.mutate({
      contadoFinal: contadoNum,
      observaciones: observaciones || undefined,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[95vw] sm:max-w-lg rounded-2xl border-t-2 border-t-rose-800/70">
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
              <Wallet className="size-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div>
              <DialogTitle>Cerrar turno de caja</DialogTitle>
              <DialogDescription>
                Finaliza el turno y cuadra la caja
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-5 py-2">
          <div className="rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 p-4">
            <div className="grid grid-cols-3 gap-4">
              <div>
                <span className="flex items-center gap-1 text-[10px] uppercase tracking-wider font-semibold text-zinc-500 mb-0.5">
                  <DollarSign className="size-3" />
                  Fondo
                </span>
                <p className="text-sm font-bold text-zinc-800 dark:text-white tabular-nums">
                  {FORMAT_CURRENCY.format(fondo)}
                </p>
              </div>
              <div>
                <span className="flex items-center gap-1 text-[10px] uppercase tracking-wider font-semibold text-emerald-600 mb-0.5">
                  <ArrowUpRight className="size-3" />
                  Ingresos
                </span>
                <p className="text-sm font-bold text-emerald-600 dark:text-emerald-400 tabular-nums">
                  {FORMAT_CURRENCY.format(ingresos)}
                </p>
              </div>
              <div>
                <span className="flex items-center gap-1 text-[10px] uppercase tracking-wider font-semibold text-red-600 mb-0.5">
                  <ArrowDownRight className="size-3" />
                  Gastos
                </span>
                <p className="text-sm font-bold text-red-600 dark:text-red-400 tabular-nums">
                  {FORMAT_CURRENCY.format(egresos)}
                </p>
              </div>
            </div>
            <div className="mt-3 pt-3 border-t border-zinc-200 dark:border-zinc-700 flex items-center justify-between">
              <span className="text-xs font-semibold text-zinc-700 dark:text-zinc-300 uppercase tracking-wider">
                Esperado de cierre
              </span>
              <span className="text-lg font-bold text-zinc-900 dark:text-white tabular-nums">
                {FORMAT_CURRENCY.format(esperado)}
              </span>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="contado" className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">
              Conteo final en caja <span className="text-red-500">*</span>
            </Label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-bold text-rose-500 z-10">
                $
              </span>
              <Input
                id="contado"
                type="text"
                inputMode="numeric"
                placeholder="0"
                value={contadoFinal}
                onChange={(e) => {
                  const v = e.target.value.replace(/[^0-9]/g, "");
                  setContadoFinal(v ? Number(v).toLocaleString("es-CO") : "");
                }}
                className="pl-10 h-12 rounded-xl text-lg font-bold tabular-nums text-zinc-800 dark:text-white"
              />
            </div>
          </div>

          {hayDiferencia && (
            <div className={`flex items-start gap-3 rounded-xl border p-4 ${
              diferencia > 0
                ? "border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/30"
                : "border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30"
            }`}>
              <AlertTriangle className={`size-5 shrink-0 mt-0.5 ${
                diferencia > 0 ? "text-amber-600" : "text-red-600"
              }`} />
              <div>
                <p className={`text-sm font-semibold ${
                  diferencia > 0 ? "text-amber-800 dark:text-amber-300" : "text-red-800 dark:text-red-300"
                }`}>
                  Diferencia detectada
                </p>
                <p className={`text-sm ${
                  diferencia > 0 ? "text-amber-700 dark:text-amber-400" : "text-red-700 dark:text-red-400"
                }`}>
                  {diferencia > 0
                    ? `Sobrante de ${FORMAT_CURRENCY.format(diferencia)}`
                    : `Faltante de ${FORMAT_CURRENCY.format(Math.abs(diferencia))}`}
                </p>
              </div>
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="obs" className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
              Observaciones <span className="text-muted-foreground font-normal">(opcional)</span>
            </Label>
            <textarea
              id="obs"
              placeholder="Notas sobre el cierre..."
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              className="flex w-full rounded-xl border border-input bg-background px-4 py-2.5 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 resize-none"
              rows={2}
            />
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex-1 h-11 rounded-xl"
            >
              Cancelar
            </Button>
            <Button
              variant="hotel"
              onClick={handleSubmit}
              disabled={cerrarMutation.isPending || !contadoFinal}
              className="flex-1 h-11 rounded-xl"
            >
              {cerrarMutation.isPending ? (
                <>
                  <Loader2 className="size-4 animate-spin mr-2" />
                  Cerrando...
                </>
              ) : (
                <>
                  Cerrar turno
                  <ArrowRight className="size-4 ml-1.5" />
                </>
              )}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
