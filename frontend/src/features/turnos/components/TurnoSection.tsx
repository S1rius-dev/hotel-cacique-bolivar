import { useState } from "react";
import {
  Clock,
  Wallet,
  User,
  CalendarDays,
  DollarSign,
  ArrowUpRight,
  ArrowDownRight,
  PiggyBank,
  LogOut,
} from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { useTurnoActivo } from "../hooks/useTurnoActivo";
import { turnoDesdeDTO, formatHoraAMPM } from "../constants/turno.constants";
import AbrirTurnoModal from "./AbrirTurnoModal";
import CerrarTurnoModal from "./CerrarTurnoModal";
import { formatDate } from "@/utils/date";

export default function TurnoSection() {
  const { data, isLoading } = useTurnoActivo();
  const [showAbrir, setShowAbrir] = useState(false);
  const [showCerrar, setShowCerrar] = useState(false);

  const turno = turnoDesdeDTO(data);

  if (isLoading) {
    return (
      <div className="h-20 rounded-xl bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-zinc-800 animate-pulse" />
    );
  }

  if (!data) {
    return (
      <>
        <div className="rounded-xl border border-dashed border-rose-200 dark:border-rose-900 bg-rose-50/50 dark:bg-rose-950/20 p-5 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center">
              <Clock className="size-5 text-rose-600 dark:text-rose-400" />
            </div>
            <div>
              <p className="text-sm font-semibold text-zinc-800 dark:text-white">
                No hay turno activo
              </p>
              <p className="text-xs text-zinc-500 dark:text-zinc-400">
                Abre un turno para registrar ingresos y gastos
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setShowAbrir(true)}
            className="
              inline-flex items-center gap-2 px-5 py-2.5 rounded-xl
              bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold
              transition-colors cursor-pointer shrink-0
            ">
            <Wallet className="size-4" />
            Abrir turno
          </button>
        </div>

        <AbrirTurnoModal open={showAbrir} onOpenChange={setShowAbrir} />
      </>
    );
  }

  const fecha = data.fechaApertura ? formatDate(data.fechaApertura) : "";
  const hora = data.fechaApertura
    ? formatHoraAMPM(
        new Date(data.fechaApertura).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";

  const fondo = data.fondoInicial ?? 0;
  const ingresos = data.totalIngresosEfectivo ?? 0;
  const egresos = data.totalEgresos ?? 0;
  const esperado = data.esperadoCierre ?? 0;

  return (
    <>
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/40 p-5 space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-3 min-w-0">
            <div className="size-10 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">
              <Wallet className="size-5 text-emerald-600 dark:text-emerald-400" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <p className="text-sm font-semibold text-zinc-800 dark:text-white">
                  Turno {turno?.info.label ?? data.tipoTurnoDisplay}
                </p>
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold uppercase tracking-wider bg-emerald-100 dark:bg-emerald-900/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                  Activo
                </span>
              </div>
              <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-zinc-500 dark:text-zinc-400 mt-1">
                <span className="flex items-center gap-1">
                  <User className="size-3" />
                  {data.usuarioApertura}
                </span>
                <span className="flex items-center gap-1">
                  <CalendarDays className="size-3" />
                  {fecha} {hora}
                </span>
              </div>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setShowCerrar(true)}
            className="
              inline-flex items-center gap-2 px-4 py-2 rounded-xl
              bg-amber-100 dark:bg-amber-900/30
              text-amber-700 dark:text-amber-300
              hover:bg-amber-200 dark:hover:bg-amber-900/50
              text-xs font-semibold
              transition-colors cursor-pointer shrink-0
            ">
            <LogOut className="size-3.5" />
            Cerrar turno
          </button>
        </div>

        {/* Grid resumen */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 p-3">
            <div className="flex items-center gap-1.5 text-zinc-500 mb-1">
              <DollarSign className="size-3" />
              <span className="text-[10px] uppercase tracking-wider font-medium">
                Fondo
              </span>
            </div>
            <p className="text-base font-bold text-zinc-800 dark:text-white">
              {FORMAT_CURRENCY.format(fondo)}
            </p>
          </div>

          <div className="rounded-lg border border-emerald-100 dark:border-emerald-900 bg-emerald-50 dark:bg-emerald-950/30 p-3">
            <div className="flex items-center gap-1.5 text-emerald-600 mb-1">
              <ArrowUpRight className="size-3" />
              <span className="text-[10px] uppercase tracking-wider font-medium">
                Ingresos
              </span>
            </div>
            <p className="text-base font-bold text-emerald-700 dark:text-emerald-300">
              {FORMAT_CURRENCY.format(ingresos)}
            </p>
          </div>

          <div className="rounded-lg border border-red-100 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3">
            <div className="flex items-center gap-1.5 text-red-600 mb-1">
              <ArrowDownRight className="size-3" />
              <span className="text-[10px] uppercase tracking-wider font-medium">
                Egresos
              </span>
            </div>
            <p className="text-base font-bold text-red-700 dark:text-red-300">
              {FORMAT_CURRENCY.format(egresos)}
            </p>
          </div>

          <div className="rounded-lg border border-blue-100 dark:border-blue-900 bg-blue-50 dark:bg-blue-950/30 p-3">
            <div className="flex items-center gap-1.5 text-blue-600 mb-1">
              <PiggyBank className="size-3" />
              <span className="text-[10px] uppercase tracking-wider font-medium">
                Esperado
              </span>
            </div>
            <p className="text-base font-bold text-blue-700 dark:text-blue-300">
              {FORMAT_CURRENCY.format(esperado)}
            </p>
          </div>
        </div>
      </div>

      <CerrarTurnoModal
        open={showCerrar}
        onOpenChange={setShowCerrar}
        turno={data}
      />
    </>
  );
}
