import { useState, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { useTurnoActivo } from "@/features/turnos";
import type { CajaNombre } from "@/features/turnos/constants/turno.constants";
import { CAJAS, turnoDesdeDTO, formatHoraAMPM } from "@/features/turnos/constants/turno.constants";
import {
  TurnoStatusBar,
  MovimientosTable,
  useMovimientosTurno,
  useMovimientosSinTurno,
  useTodosLosMovimientos,
} from "@/features/caja";
import HistorialTurnosTable from "@/features/turnos/components/HistorialTurnosTable";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { TurnoResponse } from "@/api/generated/openAPIDefinition.schemas";
import {
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  Building2,
  Store,
  ChevronLeft,
  AlertTriangle,
  Eye,
  EyeOff,
} from "lucide-react";
import { formatDate } from "@/utils/date";

const CAJA_ICON: Record<CajaNombre, typeof Building2> = {
  RECEPCION: Building2,
  TIENDA: Store,
};

function isCajaNombre(v: string | null): v is CajaNombre {
  return v === "RECEPCION" || v === "TIENDA";
}

type FiltroVista = "todos" | "turno";

interface VistaTurnoState {
  tipo: "turno";
  turno: TurnoResponse;
}

type VistaState = { tipo: "general" } | VistaTurnoState;

function SummaryCards({
  ingresos,
  gastos,
  sinTurnoCount,
}: {
  ingresos: number;
  gastos: number;
  sinTurnoCount: number;
}) {
  const balance = ingresos - gastos;
  const cards = [
    {
      label: "Ingresos",
      value: FORMAT_CURRENCY.format(ingresos),
      icon: ArrowUpRight,
      color: "text-emerald-600 dark:text-emerald-400",
      bg: "bg-emerald-50 dark:bg-emerald-950/30",
      border: "border-emerald-200 dark:border-emerald-900",
    },
    {
      label: "Gastos",
      value: FORMAT_CURRENCY.format(gastos),
      icon: ArrowDownRight,
      color: "text-red-600 dark:text-red-400",
      bg: "bg-red-50 dark:bg-red-950/30",
      border: "border-red-200 dark:border-red-900",
    },
    {
      label: "Sin turno",
      value: sinTurnoCount.toString(),
      icon: AlertTriangle,
      color: sinTurnoCount > 0 ? "text-amber-600 dark:text-amber-400" : "text-zinc-500 dark:text-zinc-400",
      bg: sinTurnoCount > 0 ? "bg-amber-50 dark:bg-amber-950/30" : "bg-zinc-50 dark:bg-zinc-900/50",
      border: sinTurnoCount > 0 ? "border-amber-200 dark:border-amber-900" : "border-zinc-200 dark:border-zinc-800",
    },
    {
      label: "Balance",
      value: `${balance >= 0 ? "+" : ""}${FORMAT_CURRENCY.format(balance)}`,
      icon: Wallet,
      color: balance >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-red-600 dark:text-red-400",
      bg: "bg-white dark:bg-zinc-900/40",
      border: "border-zinc-200 dark:border-zinc-800",
    },
  ];

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {cards.map((c) => (
        <div
          key={c.label}
          className={`rounded-xl border ${c.border} ${c.bg} p-3 flex items-center gap-3`}>
          <div className={`size-9 rounded-lg ${c.bg} flex items-center justify-center shrink-0`}>
            <c.icon className={`size-4 ${c.color}`} />
          </div>
          <div className="min-w-0">
            <p className="text-[10px] uppercase tracking-wider font-medium text-zinc-500 dark:text-zinc-400">
              {c.label}
            </p>
            <p className={`text-sm font-bold tabular-nums ${c.color}`}>
              {c.value}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}

function VistaTurnoEspecifico({
  turno,
  onVolver,
}: {
  turno: TurnoResponse;
  onVolver: () => void;
}) {
  const info = turnoDesdeDTO(turno);
  const fecha = turno.fechaApertura ? formatDate(turno.fechaApertura) : "";
  const hora = turno.fechaApertura
    ? formatHoraAMPM(
        new Date(turno.fechaApertura).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";
  const horaCierre = turno.fechaCierre
    ? formatHoraAMPM(
        new Date(turno.fechaCierre).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";
  const movimientos = turno.movimientos ?? [];

  return (
    <div className="space-y-4 md:space-y-6">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <button
          onClick={onVolver}
          className="flex items-center gap-1 hover:text-zinc-800 dark:hover:text-zinc-200 transition-colors cursor-pointer">
          <ChevronLeft className="size-3.5" />
          Volver
        </button>
        <span>/</span>
        <span className="text-zinc-700 dark:text-zinc-300 font-medium">
          Turno {info?.info.label ?? turno.tipoTurnoDisplay} — {fecha}
        </span>
        {turno.estado === "ABIERTO" && (
          <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-emerald-100 dark:bg-emerald-900/40 text-emerald-700 dark:text-emerald-300">
            Abierto
          </span>
        )}
      </div>

      {/* Turno Info Card */}
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/40 p-5">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="size-10 rounded-xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center shrink-0">
              <Wallet className="size-5 text-rose-600 dark:text-rose-400" />
            </div>
            <div>
              <p className="text-sm font-semibold text-zinc-800 dark:text-white">
                Turno {info?.info.label ?? turno.tipoTurnoDisplay}
              </p>
              <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
                <span>{turno.usuarioApertura}</span>
                <span>
                  {fecha} {hora}
                  {horaCierre && ` → ${horaCierre}`}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 mt-4">
          <div className="rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 p-2.5">
            <p className="text-[10px] uppercase tracking-wider font-medium text-zinc-500 mb-0.5">Fondo</p>
            <p className="text-sm font-bold text-zinc-800 dark:text-white">{FORMAT_CURRENCY.format(turno.fondoInicial ?? 0)}</p>
          </div>
          <div className="rounded-lg border border-emerald-100 dark:border-emerald-900 bg-emerald-50 dark:bg-emerald-950/30 p-2.5">
            <p className="text-[10px] uppercase tracking-wider font-medium text-emerald-600 mb-0.5">Ingresos</p>
            <p className="text-sm font-bold text-emerald-700 dark:text-emerald-300">{FORMAT_CURRENCY.format(turno.totalIngresosEfectivo ?? 0)}</p>
          </div>
          <div className="rounded-lg border border-red-100 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-2.5">
            <p className="text-[10px] uppercase tracking-wider font-medium text-red-600 mb-0.5">Gastos</p>
            <p className="text-sm font-bold text-red-700 dark:text-red-300">{FORMAT_CURRENCY.format(turno.totalEgresos ?? 0)}</p>
          </div>
          <div className="rounded-lg border border-blue-100 dark:border-blue-900 bg-blue-50 dark:bg-blue-950/30 p-2.5">
            <p className="text-[10px] uppercase tracking-wider font-medium text-blue-600 mb-0.5">Esperado</p>
            <p className="text-sm font-bold text-blue-700 dark:text-blue-300">{FORMAT_CURRENCY.format(turno.esperadoCierre ?? 0)}</p>
          </div>
          {turno.contadoFinal != null && (
            <div className="rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 p-2.5">
              <p className="text-[10px] uppercase tracking-wider font-medium text-zinc-500 mb-0.5">Contado</p>
              <div className="flex items-baseline gap-1.5">
                <p className="text-sm font-bold text-zinc-800 dark:text-white">{FORMAT_CURRENCY.format(turno.contadoFinal)}</p>
                {turno.diferencia != null && (
                  <span className={`text-[10px] font-mono ${turno.diferencia > 0 ? "text-emerald-600" : turno.diferencia < 0 ? "text-red-600" : "text-zinc-500"}`}>
                    {turno.diferencia > 0 ? "+" : ""}{FORMAT_CURRENCY.format(turno.diferencia)}
                  </span>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Movimientos del turno */}
      <MovimientosTable
        movimientos={movimientos}
        titulo={`Movimientos del turno — ${info?.info.label ?? turno.tipoTurnoDisplay}`}
        showTurnoColumn={false}
      />
    </div>
  );
}

function CajaTabContent({ caja }: { caja: CajaNombre }) {
  const { data: turnoActivo } = useTurnoActivo({ caja });
  const { data: movimientosSinTurno } = useMovimientosSinTurno(caja);
  const {
    data: movimientosTurno,
    isLoading: loadingTurno,
    isError: errorTurno,
    error: turnoError,
  } = useMovimientosTurno(caja);
  const {
    data: todosLosMovimientos,
    isLoading: loadingTodos,
  } = useTodosLosMovimientos(caja);

  const [vista, setVista] = useState<VistaState>({ tipo: "general" });
  const [filtro, setFiltro] = useState<FiltroVista>("todos");

  // Movimientos filtrados según toggle
  const movimientosFiltrados = useMemo(() => {
    if (filtro === "turno") {
      return movimientosTurno ?? [];
    }
    return todosLosMovimientos ?? [];
  }, [filtro, todosLosMovimientos, movimientosTurno]);

  const sinTurnoCount = (movimientosSinTurno ?? []).length;

  // Resumen
  const ingresos = movimientosFiltrados
    .filter((m) => m.tipo === "INGRESO" || m.tipo === "PAGO")
    .reduce((acc, m) => acc + (m.monto ?? 0), 0);
  const gastos = movimientosFiltrados
    .filter((m) => m.tipo === "GASTO" || m.tipo === "EGRESO")
    .reduce((acc, m) => acc + (m.monto ?? 0), 0);

  const handleTurnoSelect = (turno: TurnoResponse) => {
    setVista({ tipo: "turno", turno });
  };

  const handleVolver = () => {
    setVista({ tipo: "general" });
  };

  // Vista de turno específico
  if (vista.tipo === "turno") {
    return (
      <VistaTurnoEspecifico
        turno={vista.turno}
        onVolver={handleVolver}
      />
    );
  }

  // Vista general
  return (
    <div className="space-y-4 md:space-y-6">
      <TurnoStatusBar caja={caja} />

      <SummaryCards
        ingresos={ingresos}
        gastos={gastos}
        sinTurnoCount={sinTurnoCount}
      />

      {/* Toggle de filtro */}
      <div className="flex items-center gap-2">
        <span className="text-xs text-muted-foreground">Movimientos:</span>
        <div className="flex items-center rounded-lg border border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-800/60 p-0.5">
          <button
            onClick={() => setFiltro("todos")}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-colors cursor-pointer ${
              filtro === "todos"
                ? "bg-white dark:bg-zinc-900 shadow-sm text-zinc-800 dark:text-zinc-200"
                : "text-zinc-500 dark:text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-300"
            }`}>
            <Eye className="size-3" />
            Todos
          </button>
          <button
            onClick={() => setFiltro("turno")}
            disabled={!turnoActivo}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed ${
              filtro === "turno"
                ? "bg-white dark:bg-zinc-900 shadow-sm text-zinc-800 dark:text-zinc-200"
                : "text-zinc-500 dark:text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-300"
            }`}>
            <EyeOff className="size-3" />
            Solo turno activo
          </button>
        </div>
      </div>

      <MovimientosTable
        movimientos={movimientosFiltrados}
        isLoading={filtro === "turno" ? loadingTurno : loadingTodos}
        isError={filtro === "turno" ? errorTurno : false}
        error={filtro === "turno" ? turnoError : undefined}
        titulo={filtro === "turno" ? "Movimientos del turno activo" : "Todos los movimientos"}
        showTurnoColumn={filtro === "todos"}
      />

      <HistorialTurnosTable caja={caja} onTurnoSelect={handleTurnoSelect} />
    </div>
  );
}

export default function CajaPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const raw = searchParams.get("caja");
  const caja: CajaNombre = isCajaNombre(raw) ? raw : "RECEPCION";

  const handleChange = (value: string) => {
    if (!isCajaNombre(value)) return;
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set("caja", value);
      return next;
    });
  };

  return (
    <div className="h-full overflow-y-auto overflow-x-hidden p-3 sm:p-4 md:p-6 space-y-4 md:space-y-6 min-h-0">
      <Tabs value={caja} onValueChange={handleChange} className="w-full">
        <TabsList variant="line" className="h-9 w-full sm:w-fit justify-start rounded-lg bg-zinc-100 dark:bg-zinc-800/60 p-1 sm:bg-transparent sm:dark:bg-transparent sm:p-0 sm:gap-1">
          {CAJAS.map((c) => {
            const Icon = CAJA_ICON[c.value];
            return (
              <TabsTrigger
                key={c.value}
                value={c.value}
                className="flex-1 sm:flex-none gap-1.5 rounded-md data-[state=active]:bg-white dark:data-[state=active]:bg-zinc-900 data-[state=active]:shadow-sm sm:data-[state=active]:bg-rose-50 sm:dark:data-[state=active]:bg-rose-950/30 sm:data-[state=active]:text-rose-700 sm:dark:data-[state=active]:text-rose-300 sm:data-[state=active]:border sm:data-[state=active]:border-rose-200 sm:dark:data-[state=active]:border-rose-900 px-3 py-1.5 text-xs sm:text-sm">
                <Icon className="size-3.5" />
                {c.label}
              </TabsTrigger>
            );
          })}
        </TabsList>
      </Tabs>

      <CajaTabContent key={caja} caja={caja} />
    </div>
  );
}
