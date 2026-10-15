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
import { Receipt, Loader2, ArrowUpRight, ArrowDownRight } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { useRegistrarGasto } from "../hooks/useRegistrarGasto";
import { useTurnoActivo } from "../hooks/useTurnoActivo";
import type { CajaNombre } from "../constants/turno.constants";

const CATEGORIAS = ["Papelería", "Cafetería", "Limpieza", "Transporte", "Otro"];

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  caja?: CajaNombre;
}

export default function RegistrarGastoModal({
  open,
  onOpenChange,
  caja = "RECEPCION",
}: Props) {
  const [concepto, setConcepto] = useState("");
  const [monto, setMonto] = useState("");
  const [observaciones, setObservaciones] = useState("");

  const { data: turno } = useTurnoActivo({ caja });
  const ingresos = turno?.totalIngresosEfectivo ?? 0;
  const egresos = turno?.totalEgresos ?? 0;

  const mutation = useRegistrarGasto({ caja }, () => {
    onOpenChange(false);
    setConcepto("");
    setMonto("");
    setObservaciones("");
  });

  const handleSubmit = () => {
    const montoNum = Number(monto.replace(/[^0-9]/g, ""));
    if (!concepto.trim() || !montoNum) return;
    mutation.mutate({
      concepto: concepto.trim(),
      monto: montoNum,
      observaciones: observaciones.trim() || undefined,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[95vw] sm:max-w-md rounded-2xl border-t-2 border-t-rose-800/70">
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-orange-100 dark:bg-orange-900/30 flex items-center justify-center shrink-0">
              <Receipt className="size-5 text-orange-600 dark:text-orange-400" />
            </div>
            <div>
              <DialogTitle>Registrar gasto</DialogTitle>
              <DialogDescription>
                Registra un egreso del turno activo
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-5 py-2">
          <div className="flex items-center justify-between rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 px-4 py-2.5 text-xs">
            <span className="flex items-center gap-1.5 text-muted-foreground">
              <ArrowUpRight className="size-3.5 text-emerald-600" />
              Ingresos <strong className="text-emerald-600 dark:text-emerald-400 font-bold tabular-nums">{FORMAT_CURRENCY.format(ingresos)}</strong>
            </span>
            <span className="text-muted-foreground">|</span>
            <span className="flex items-center gap-1.5 text-muted-foreground">
              <ArrowDownRight className="size-3.5 text-red-600" />
              Gastos <strong className="text-red-600 dark:text-red-400 font-bold tabular-nums">{FORMAT_CURRENCY.format(egresos)}</strong>
            </span>
          </div>

          <div className="space-y-2">
            <Label htmlFor="concepto" className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">
              Concepto <span className="text-red-500">*</span>
            </Label>
            <div className="flex flex-wrap gap-1.5 mb-2">
              {CATEGORIAS.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setConcepto(cat === "Otro" ? "" : cat)}
                  className={`text-[11px] px-2.5 py-1 rounded-full font-medium border transition-colors cursor-pointer ${
                    concepto === cat
                      ? "bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300 border-orange-200 dark:border-orange-800"
                      : "bg-zinc-50 dark:bg-zinc-900/30 text-zinc-600 dark:text-zinc-400 border-zinc-200 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                  }`}>
                  {cat}
                </button>
              ))}
            </div>
            <Input
              id="concepto"
              placeholder={concepto && concepto !== "Otro" ? concepto : "Describe el gasto..."}
              value={concepto === "Otro" ? "" : concepto}
              onChange={(e) => setConcepto(e.target.value)}
              className="h-10 rounded-xl"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="monto" className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">
              Monto <span className="text-red-500">*</span>
            </Label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-bold text-rose-500 z-10">
                $
              </span>
              <Input
                id="monto"
                type="text"
                inputMode="numeric"
                placeholder="0"
                value={monto}
                onChange={(e) => {
                  const v = e.target.value.replace(/[^0-9]/g, "");
                  setMonto(v ? Number(v).toLocaleString("es-CO") : "");
                }}
                className="pl-10 h-12 rounded-xl text-lg font-bold tabular-nums text-zinc-800 dark:text-white"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="observaciones" className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
              Observaciones <span className="text-muted-foreground font-normal">(opcional)</span>
            </Label>
            <textarea
              id="observaciones"
              placeholder="Notas adicionales..."
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
              disabled={
                mutation.isPending ||
                !concepto.trim() ||
                !monto.replace(/[^0-9]/g, "")
              }
              className="flex-1 h-11 rounded-xl"
            >
              {mutation.isPending ? (
                <>
                  <Loader2 className="size-4 animate-spin mr-2" />
                  Registrando...
                </>
              ) : (
                "Registrar gasto"
              )}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
