import { useState } from "react";
import {
  useTurnoActivo,
  turnoDesdeDTO,
  formatHoraAMPM,
  formatUsuario,
} from "@/features/turnos";
import AbrirTurnoModal from "@/features/turnos/components/AbrirTurnoModal";
import CerrarTurnoModal from "@/features/turnos/components/CerrarTurnoModal";
import RegistrarGastoModal from "@/features/turnos/components/RegistrarGastoModal";
import { FORMAT_CURRENCY } from "@/features/pagos";
import {
  Wallet, Clock, User, CalendarDays, LogOut, Receipt, Loader2,
} from "lucide-react";
import { formatDate } from "@/utils/date";
import type { CajaNombre } from "@/features/turnos/constants/turno.constants";

export default function TurnoStatusBar({ caja = "RECEPCION" }: { caja?: CajaNombre }) {
  const { data, isLoading } = useTurnoActivo({ caja });
  const turnoInfo = data ? turnoDesdeDTO(data) : null;
  const [showAbrir, setShowAbrir] = useState(false);
  const [showCerrar, setShowCerrar] = useState(false);
  const [showGasto, setShowGasto] = useState(false);

  const fecha = data?.fechaApertura ? formatDate(data.fechaApertura) : "";
  const hora = data?.fechaApertura
    ? formatHoraAMPM(
        new Date(data.fechaApertura).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";

  const fondo = data?.fondoInicial ?? 0;
  const ingresos = data?.totalIngresosEfectivo ?? 0;
  const egresos = data?.totalEgresos ?? 0;
  const esperado = data?.esperadoCierre ?? 0;

  if (isLoading) {
    return (
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/40 p-4 flex items-center justify-center">
        <Loader2 className="size-5 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!data) {
    return (
      <>
        <div className="rounded-xl border border-dashed border-rose-200 dark:border-rose-900 bg-rose-50/50 dark:bg-rose-950/20 p-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center shrink-0">
              <Clock className="size-5 text-rose-600 dark:text-rose-400" />
            </div>
            <div>
              <p className="text-sm font-semibold text-zinc-800 dark:text-white">
                No hay turno activo
              </p>
              <p className="text-xs text-zinc-500 dark:text-zinc-400">
                Abre un turno para comenzar a registrar movimientos
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setShowAbrir(true)}
            className="inline-flex items-center gap-2 px-5 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold transition-colors cursor-pointer shrink-0">
            <Wallet className="size-4" />
            Abrir turno
          </button>
        </div>
        <AbrirTurnoModal open={showAbrir} onOpenChange={setShowAbrir} caja={caja} />
      </>
    );
  }

  const metrics = [
    { label: "Fondo inicial", value: FORMAT_CURRENCY.format(fondo) },
    { label: "Ingresos", value: FORMAT_CURRENCY.format(ingresos), positive: true },
    { label: "Gastos", value: FORMAT_CURRENCY.format(egresos), negative: true },
    { label: "Esperado", value: FORMAT_CURRENCY.format(esperado) },
  ];

  return (
    <>
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 border-t-2 border-t-rose-800/70 bg-white dark:bg-zinc-900/40 divide-y divide-zinc-100 dark:divide-zinc-800">
        <div className="flex items-center justify-between gap-4 px-5 py-3">
          <div className="flex items-center gap-3">
            <div className="size-9 rounded-lg bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center shrink-0">
              <Wallet className="size-4 text-rose-600 dark:text-rose-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <p className="text-sm font-semibold text-zinc-800 dark:text-white">
                  Turno {turnoInfo?.info.label ?? data.tipoTurnoDisplay}
                </p>
                <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-emerald-100 dark:bg-emerald-900/40 text-emerald-700 dark:text-emerald-300">
                  Activo
                </span>
              </div>
              <div className="flex items-center gap-3 text-[11px] text-zinc-500 dark:text-zinc-400 mt-0.5">
                <span className="flex items-center gap-1">
                  <User className="size-3" />
                  {formatUsuario(data.usuarioApertura)}
                </span>
                <span className="flex items-center gap-1">
                  <CalendarDays className="size-3" />
                  {fecha} {hora}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setShowGasto(true)}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300 hover:bg-orange-200 dark:hover:bg-orange-900/50 text-xs font-semibold transition-colors cursor-pointer">
              <Receipt className="size-3.5" />
              Gasto
            </button>
            <button
              type="button"
              onClick={() => setShowCerrar(true)}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-amber-900/50 text-xs font-semibold transition-colors cursor-pointer">
              <LogOut className="size-3.5" />
              Cerrar
            </button>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-x-6 gap-y-1 px-5 py-2.5">
          {metrics.map((m) => (
            <div key={m.label} className="flex items-baseline gap-1.5">
              <span
                className={`text-base font-bold tabular-nums ${
                  m.positive
                    ? "text-emerald-600 dark:text-emerald-400"
                    : m.negative
                      ? "text-red-600 dark:text-red-400"
                      : "text-zinc-800 dark:text-white"
                }`}>
                {m.value}
              </span>
              <span className="text-[11px] text-muted-foreground">{m.label}</span>
            </div>
          ))}
        </div>
      </div>

      <CerrarTurnoModal open={showCerrar} onOpenChange={setShowCerrar} turno={data} caja={caja} />
      <RegistrarGastoModal open={showGasto} onOpenChange={setShowGasto} caja={caja} />
    </>
  );
}
