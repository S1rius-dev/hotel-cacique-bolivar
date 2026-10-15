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
import { Wallet, Loader2, Clock, User, CalendarDays, ArrowRight } from "lucide-react";
import { useAbrirTurno } from "../hooks/useTurnoMutations";
import { obtenerTurnoActual } from "../constants/turno.constants";
import type { CajaNombre } from "../constants/turno.constants";
import { useAuth } from "@/app/auth/AuthContext";
import { formatDate } from "@/utils/date";

function formatUsuario(value?: string | null): string {
  if (!value) return "-";
  if (value.includes("@")) return value.split("@")[0];
  return value;
}

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  caja?: CajaNombre;
}

export default function AbrirTurnoModal({
  open,
  onOpenChange,
  caja = "RECEPCION",
}: Props) {
  const [fondoInicial, setFondoInicial] = useState("");
  const { user } = useAuth();
  const turnoActual = obtenerTurnoActual();

  const cajaLabel = caja === "TIENDA" ? "tienda" : "recepción";
  const abrirMutation = useAbrirTurno({ caja }, () => {
    onOpenChange(false);
    setFondoInicial("");
  });

  const handleSubmit = () => {
    const monto = Number(fondoInicial.replace(/[^0-9]/g, ""));
    if (isNaN(monto)) return;
    abrirMutation.mutate({ fondoInicial: monto });
  };

  const hoy = formatDate(new Date());

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[95vw] sm:max-w-md rounded-2xl border-t-2 border-t-rose-800/70">
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center shrink-0">
              <Wallet className="size-5 text-rose-600 dark:text-rose-400" />
            </div>
            <div>
              <DialogTitle>Abrir turno de caja</DialogTitle>
              <DialogDescription>
                Inicia la jornada laboral en caja {cajaLabel}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-5 py-2">
          <div className="rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 p-4 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-[11px] text-muted-foreground uppercase tracking-wider font-semibold">
                Turno
              </span>
              <span className="inline-flex items-center gap-1.5 text-xs font-bold text-rose-700 dark:text-rose-300 bg-rose-100 dark:bg-rose-900/30 px-2 py-1 rounded-md">
                <Clock className="size-3.5" />
                {turnoActual.info.label}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted-foreground">Horario</span>
              <span className="text-zinc-700 dark:text-zinc-300 font-medium">
                {turnoActual.info.inicio} — {turnoActual.info.fin}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted-foreground">Usuario</span>
              <span className="flex items-center gap-1 text-zinc-700 dark:text-zinc-300 font-medium">
                <User className="size-3" />
                {formatUsuario(user?.username ?? user?.email)}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted-foreground">Fecha</span>
              <span className="flex items-center gap-1 text-zinc-700 dark:text-zinc-300 font-medium">
                <CalendarDays className="size-3" />
                {hoy}
              </span>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="fondo" className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">
              Fondo inicial
            </Label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-bold text-rose-500 z-10">
                $
              </span>
              <Input
                id="fondo"
                type="text"
                inputMode="numeric"
                placeholder="0"
                value={fondoInicial}
                onChange={(e) => {
                  const v = e.target.value.replace(/[^0-9]/g, "");
                  setFondoInicial(v ? Number(v).toLocaleString("es-CO") : "");
                }}
                className="pl-10 h-12 rounded-xl text-lg font-bold tabular-nums text-zinc-800 dark:text-white"
              />
            </div>
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
              disabled={abrirMutation.isPending}
              className="flex-1 h-11 rounded-xl"
            >
              {abrirMutation.isPending ? (
                <>
                  <Loader2 className="size-4 animate-spin mr-2" />
                  Abriendo...
                </>
              ) : (
                <>
                  Abrir turno
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
