import { useState, useMemo, useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { AnimatePresence } from "motion/react";
import OcupacionHeader from "../components/OcupacionHeader";
import CalendarPanel from "../components/CalendarPanel";
import RoomGrid from "../components/RoomGrid";
import RoomGridSkeleton from "../components/RoomGridSkeleton";
import AccionesMasivasBar from "../components/AccionesMasivasBar";
import QuickReservaModal from "../components/QuickReservaModal";
import CheckInModal from "../components/CheckInModal";
import CheckOutModal from "../components/CheckOutModal";
import CleaningModal from "../components/CleaningModal";
import ExtenderEstadiaModal from "../components/ExtenderEstadiaModal";
import RoomDetailModal from "../components/RoomDetailModal";
import { Sheet, SheetContent } from "@/components/ui/sheet";
import { useEstadoDiario } from "../hooks/useEstadoDiario";
import { useLimpiarVarias } from "../hooks/useLimpiarVarias";
import { useTurnoActivo } from "@/features/turnos";
import { FORMAT_CURRENCY } from "@/features/pagos";
import {
  SprayCan,
  Wallet,
  ArrowUpRight,
  PiggyBank,
  ShoppingBag,
} from "lucide-react";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";
import VenderProductoDrawer from "@/features/tienda/components/VenderProductoDrawer";
import { useIsMobile } from "@/lib/useMediaQuery";

function OcupacionPage() {
  const navigate = useNavigate();
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [calendarOpen, setCalendarOpen] = useState(
    () => sessionStorage.getItem("ocupacion:calendarOpen") !== "false",
  );
  const isMobile = useIsMobile();

  const [asignarHabitacion, setAsignarHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);
  const [checkInHabitacion, setCheckInHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);
  const [checkOutHabitacion, setCheckOutHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);
  const [limpiezaHabitacion, setLimpiezaHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);
  const [detalleHabitacion, setDetalleHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);

  useEffect(() => {
    sessionStorage.setItem("ocupacion:calendarOpen", String(calendarOpen));
  }, [calendarOpen]);

  const [extenderHabitacion, setExtenderHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);
  const [venderHabitacion, setVenderHabitacion] =
    useState<HabitacionEstadoDiario | null>(null);

  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedRoomIds, setSelectedRoomIds] = useState<Set<number>>(
    new Set(),
  );
  const limpiarVarias = useLimpiarVarias();

  const handleToggleSelect = useCallback((habitacionId: number) => {
    setSelectedRoomIds((prev) => {
      const next = new Set(prev);
      if (next.has(habitacionId)) next.delete(habitacionId);
      else next.add(habitacionId);
      return next;
    });
  }, []);

  const handleDeselectAll = useCallback(() => {
    setSelectedRoomIds(new Set());
  }, []);

  const handleCancelSeleccion = useCallback(() => {
    setSelectionMode(false);
    setSelectedRoomIds(new Set());
  }, []);

  const handleNavigateReserva = useCallback(
    (reservaId: number) => {
      setDetalleHabitacion(null);
      navigate(`/dashboard/reservas/${reservaId}`);
    },
    [navigate],
  );

  const { data: turnoData, refetch: refetchTurno } = useTurnoActivo();

  useEffect(() => {
    refetchTurno();
  }, [selectedDate, refetchTurno]);

  const {
    data: habitaciones,
    isLoading,
    isError,
  } = useEstadoDiario(selectedDate);

  /**
   * Al refrescar `habitaciones`, los modales abiertos siguen apuntando a los
   * objetos de la carga anterior. Se re-sincronizan con la version nueva. El efecto
   * es idempotente: solo escribe si la referencia cambió de verdad.
   */
  useEffect(() => {
    if (!habitaciones) return;
    [
      [setAsignarHabitacion, asignarHabitacion] as const,
      [setCheckInHabitacion, checkInHabitacion] as const,
      [setCheckOutHabitacion, checkOutHabitacion] as const,
      [setLimpiezaHabitacion, limpiezaHabitacion] as const,
      [setDetalleHabitacion, detalleHabitacion] as const,
      [setExtenderHabitacion, extenderHabitacion] as const,
      [setVenderHabitacion, venderHabitacion] as const,
    ].forEach(([setter, current]) => {
      if (!current) return;
      const updated = habitaciones.find((h) => h.id === current.id);
      if (updated && updated !== current) {
        setter(updated);
      }
    });
  }, [
    habitaciones,
    asignarHabitacion,
    checkInHabitacion,
    checkOutHabitacion,
    limpiezaHabitacion,
    detalleHabitacion,
    extenderHabitacion,
    venderHabitacion,
  ]);

  const [tipoFilter, setTipoFilter] = useState<string>("");

  const isPastDate = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return selectedDate < today;
  }, [selectedDate]);

  const isToday = useMemo(() => {
    const today = new Date();
    return (
      selectedDate.getDate() === today.getDate() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getFullYear() === today.getFullYear()
    );
  }, [selectedDate]);

  const tipos = useMemo(() => {
    const unique = new Set(
      (habitaciones ?? [])
        .map((h) => h.tipoNombre)
        .filter((t): t is string => !!t),
    );
    return Array.from(unique).sort();
  }, [habitaciones]);

  const filteredHabitaciones = useMemo(() => {
    if (!tipoFilter) return habitaciones ?? [];
    return (habitaciones ?? []).filter((h) => h.tipoNombre === tipoFilter);
  }, [habitaciones, tipoFilter]);

  const stats = useMemo(
    () => ({
      disponibles: filteredHabitaciones.filter((h) => h.estado === "DISPONIBLE")
        .length,
      ocupadas: filteredHabitaciones.filter((h) => h.estado === "OCUPADA")
        .length,
      reservadas: filteredHabitaciones.filter((h) => h.estado === "RESERVADA")
        .length,
      enLimpieza: filteredHabitaciones.filter((h) => h.estado === "EN_LIMPIEZA")
        .length,
    }),
    [filteredHabitaciones],
  );

  const selectionHabitaciones = useMemo(
    () =>
      selectionMode
        ? filteredHabitaciones.filter((h) => h.estado === "EN_LIMPIEZA")
        : filteredHabitaciones,
    [filteredHabitaciones, selectionMode],
  );

  const handleSelectAllEnLimpieza = useCallback(() => {
    const ids = selectionHabitaciones.map((h) => h.id!).filter(Boolean) ?? [];
    setSelectedRoomIds(new Set(ids));
  }, [selectionHabitaciones]);

  const handleMarcarLimpias = useCallback(async () => {
    const ids = Array.from(selectedRoomIds);
    if (ids.length === 0) return;
    try {
      await limpiarVarias.mutateAsync(ids);
      setSelectionMode(false);
      setSelectedRoomIds(new Set());
    } catch {
      /* error toast handled by hook */
    }
  }, [selectedRoomIds, limpiarVarias]);

  const handleToggleSeleccion = useCallback(() => {
    if (selectionMode) {
      setSelectionMode(false);
      setSelectedRoomIds(new Set());
      return;
    }
    setSelectionMode(true);
    setSelectedRoomIds(new Set());
  }, [selectionMode]);

  return (
    <div className="h-full flex flex-col overflow-hidden">
      <OcupacionHeader
        selectedDate={selectedDate}
        calendarOpen={calendarOpen}
        onToggleCalendar={() => setCalendarOpen((v) => !v)}
        stats={stats}
        tipos={tipos}
        tipoFilter={tipoFilter}
        onTipoChange={setTipoFilter}
        readOnly={isPastDate}
      />

      <div className="flex-1 min-h-0 overflow-hidden">
        {/* Desktop: layout with inline calendar sidebar */}
        <div
          className="hidden md:grid h-full"
          style={{
            gridTemplateColumns: calendarOpen ? "260px 1fr" : "1fr",
          }}>
          {calendarOpen && (
            <div className="min-h-0 overflow-hidden border-r border-zinc-200 dark:border-zinc-800">
              <CalendarPanel
                selectedDate={selectedDate}
                onDateChange={(date) => date && setSelectedDate(date)}
                onClose={() => setCalendarOpen(false)}
              />
            </div>
          )}

          <div className="overflow-y-auto min-w-0 p-6 pt-2">
            {!isLoading && !isError && (
              <div className="flex flex-wrap items-center gap-2 mb-4">
                {turnoData && (
                  <>
                    <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1.5 rounded-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 font-medium shadow-sm">
                      <Wallet className="size-3 text-rose-500" />
                      Fondo:{" "}
                      {FORMAT_CURRENCY.format(turnoData.fondoInicial ?? 0)}
                    </span>
                    <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1.5 rounded-full bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900 text-emerald-700 dark:text-emerald-300 font-medium shadow-sm">
                      <ArrowUpRight className="size-3" />
                      Recaudado:{" "}
                      {FORMAT_CURRENCY.format(
                        turnoData.totalIngresosEfectivo ?? 0,
                      )}
                    </span>
                    <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1.5 rounded-full bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900 text-blue-700 dark:text-blue-300 font-medium shadow-sm">
                      <PiggyBank className="size-3" />
                      En caja:{" "}
                      {FORMAT_CURRENCY.format(turnoData.esperadoCierre ?? 0)}
                    </span>
                  </>
                )}
                {isToday && (
                  <button
                    onClick={handleToggleSeleccion}
                    disabled={stats.enLimpieza === 0}
                    className={`inline-flex items-center gap-1.5 text-[11px] px-3 py-1.5 rounded-full font-medium shadow-sm transition-colors border ${
                      selectionMode
                        ? "bg-sky-700 text-white border-sky-600"
                        : stats.enLimpieza === 0
                          ? "bg-sky-50/50 dark:bg-sky-950/20 text-sky-400 dark:text-sky-600 border-sky-200/50 dark:border-sky-800/50 cursor-not-allowed"
                          : "bg-sky-50 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 border-sky-200 dark:border-sky-800 hover:bg-sky-100 dark:hover:bg-sky-900/40"
                    }`}>
                    <SprayCan
                      className={`size-3 ${selectionMode ? "text-white" : stats.enLimpieza === 0 ? "text-sky-300 dark:text-sky-600" : "text-sky-500"}`}
                    />
                    {selectionMode ? "Salir limpieza" : "Limpieza múltiple"}
                  </button>
                )}
              </div>
            )}

            <AnimatePresence>
              {selectionMode && (
                <AccionesMasivasBar
                  selectedCount={selectedRoomIds.size}
                  totalCount={selectionHabitaciones.length}
                  onSelectAll={handleSelectAllEnLimpieza}
                  onDeselectAll={handleDeselectAll}
                  onCancel={handleCancelSeleccion}
                  onMarcarLimpias={handleMarcarLimpias}
                  isPending={limpiarVarias.isPending}
                />
              )}
            </AnimatePresence>

            {isLoading || isError ? (
              <RoomGridSkeleton />
            ) : (
              <RoomGrid
                selectionMode={selectionMode}
                selectedRoomIds={selectedRoomIds}
                onToggleSelect={handleToggleSelect}
                habitaciones={selectionHabitaciones}
                selectedDate={selectedDate}
                onAsignar={
                  selectionMode || isPastDate
                    ? undefined
                    : (h) => setAsignarHabitacion(h)
                }
                onCheckIn={
                  selectionMode || isPastDate
                    ? undefined
                    : (h) => setCheckInHabitacion(h)
                }
                onCheckOut={
                  selectionMode || isPastDate
                    ? undefined
                    : (h) => setCheckOutHabitacion(h)
                }
                onLimpiar={
                  isPastDate ? undefined : (h) => setLimpiezaHabitacion(h)
                }
                onExtender={
                  selectionMode || isPastDate
                    ? undefined
                    : (h) => setExtenderHabitacion(h)
                }
                onVerDetalle={selectionMode ? undefined : setDetalleHabitacion}
                onVender={
                  selectionMode || isPastDate
                    ? undefined
                    : (h) => setVenderHabitacion(h)
                }
              />
            )}
          </div>
        </div>

        {/* Mobile: full-width layout (calendar opens in Sheet) */}
        <div className="md:hidden h-full overflow-y-auto p-4 pt-2">
          {!isLoading && !isError && (
            <div className="flex flex-wrap items-center gap-2 mb-4">
              {turnoData && (
                <>
                  <span className="inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 font-medium shadow-sm">
                    <Wallet className="size-2.5 text-rose-500" />
                    {FORMAT_CURRENCY.format(turnoData.fondoInicial ?? 0)}
                  </span>
                  <span className="inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900 text-emerald-700 dark:text-emerald-300 font-medium shadow-sm">
                    <ArrowUpRight className="size-2.5" />
                    {FORMAT_CURRENCY.format(
                      turnoData.totalIngresosEfectivo ?? 0,
                    )}
                  </span>
                  <span className="inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-full bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900 text-blue-700 dark:text-blue-300 font-medium shadow-sm">
                    <PiggyBank className="size-2.5" />
                    {FORMAT_CURRENCY.format(turnoData.esperadoCierre ?? 0)}
                  </span>
                </>
              )}
              {habitaciones?.some(
                (h) => h.reservaActiva?.estadoReserva === "EN_CURSO",
              ) && (
                <button
                  onClick={() =>
                    setVenderHabitacion(
                      habitaciones.find(
                        (h) => h.reservaActiva?.estadoReserva === "EN_CURSO",
                      ) ?? null,
                    )
                  }
                  className="inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-full bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 font-medium hover:bg-rose-100 dark:hover:bg-rose-900/20 ml-auto shadow-sm">
                  <ShoppingBag className="size-2.5" /> Vender
                </button>
              )}
              {isToday && (
                <button
                  onClick={handleToggleSeleccion}
                  disabled={stats.enLimpieza === 0}
                  className={`inline-flex items-center gap-1.5 text-[11px] px-2.5 py-1 rounded-full font-medium shadow-sm transition-colors border ${
                    selectionMode
                      ? "bg-sky-700 text-white border-sky-600"
                      : stats.enLimpieza === 0
                        ? "bg-sky-50/50 dark:bg-sky-950/20 text-sky-400 dark:text-sky-600 border-sky-200/50 dark:border-sky-800/50 cursor-not-allowed"
                        : "bg-sky-50 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 border-sky-200 dark:border-sky-800 hover:bg-sky-100 dark:hover:bg-sky-900/40"
                  }`}>
                  <SprayCan
                    className={`size-2.5 ${selectionMode ? "text-white" : stats.enLimpieza === 0 ? "text-sky-300 dark:text-sky-600" : "text-sky-500"}`}
                  />
                  {selectionMode ? "Salir" : "🧹 Limpieza"}
                </button>
              )}
            </div>
          )}

          <AnimatePresence>
            {selectionMode && (
              <AccionesMasivasBar
                selectedCount={selectedRoomIds.size}
                totalCount={selectionHabitaciones.length}
                onSelectAll={handleSelectAllEnLimpieza}
                onDeselectAll={handleDeselectAll}
                onCancel={handleCancelSeleccion}
                onMarcarLimpias={handleMarcarLimpias}
                isPending={limpiarVarias.isPending}
              />
            )}
          </AnimatePresence>

          {isLoading || isError ? (
            <RoomGridSkeleton />
          ) : (
            <RoomGrid
              selectionMode={selectionMode}
              selectedRoomIds={selectedRoomIds}
              onToggleSelect={handleToggleSelect}
              habitaciones={selectionHabitaciones}
              selectedDate={selectedDate}
              onAsignar={
                selectionMode || isPastDate
                  ? undefined
                  : (h) => setAsignarHabitacion(h)
              }
              onCheckIn={
                selectionMode || isPastDate
                  ? undefined
                  : (h) => setCheckInHabitacion(h)
              }
              onCheckOut={
                selectionMode || isPastDate
                  ? undefined
                  : (h) => setCheckOutHabitacion(h)
              }
              onLimpiar={
                isPastDate ? undefined : (h) => setLimpiezaHabitacion(h)
              }
              onExtender={
                selectionMode || isPastDate
                  ? undefined
                  : (h) => setExtenderHabitacion(h)
              }
              onVerDetalle={selectionMode ? undefined : setDetalleHabitacion}
              onVender={
                selectionMode || isPastDate
                  ? undefined
                  : (h) => setVenderHabitacion(h)
              }
            />
          )}
        </div>
      </div>

      {isMobile && (
        <Sheet open={calendarOpen} onOpenChange={setCalendarOpen}>
          <SheetContent side="bottom" className="p-0 max-h-[85vh]">
            <CalendarPanel
              selectedDate={selectedDate}
              onDateChange={(date) => {
                if (date) {
                  setSelectedDate(date);
                  setCalendarOpen(false);
                }
              }}
              onClose={() => setCalendarOpen(false)}
              showCloseButton={false}
            />
          </SheetContent>
        </Sheet>
      )}

      <QuickReservaModal
        open={!!asignarHabitacion}
        habitacion={asignarHabitacion}
        onClose={() => {
          setAsignarHabitacion(null);
          refetchTurno();
        }}
        selectedDate={selectedDate}
      />

      <CheckInModal
        open={!!checkInHabitacion}
        habitacion={checkInHabitacion}
        onClose={() => {
          setCheckInHabitacion(null);
          refetchTurno();
        }}
      />

      <CheckOutModal
        open={!!checkOutHabitacion}
        habitacion={checkOutHabitacion}
        onClose={() => {
          setCheckOutHabitacion(null);
          refetchTurno();
        }}
      />

      <CleaningModal
        open={!!limpiezaHabitacion}
        habitacion={limpiezaHabitacion}
        onClose={() => setLimpiezaHabitacion(null)}
      />

      <ExtenderEstadiaModal
        open={!!extenderHabitacion}
        reserva={extenderHabitacion?.reservaActiva ?? null}
        onClose={() => setExtenderHabitacion(null)}
      />

      <RoomDetailModal
        open={!!detalleHabitacion}
        habitacion={detalleHabitacion}
        onClose={() => setDetalleHabitacion(null)}
        onNavigate={handleNavigateReserva}
        onVender={(h) => setVenderHabitacion(h)}
      />
      <VenderProductoDrawer
        open={!!venderHabitacion}
        onOpenChange={(v) => !v && setVenderHabitacion(null)}
        preselectedReservaId={
          venderHabitacion?.reservaActiva?.reservaId ?? null
        }
        preselectedDetalleId={
          venderHabitacion?.reservaActiva?.detalleId ?? null
        }
      />
    </div>
  );
}

export default OcupacionPage;
