import { useState, useCallback, useMemo } from "react";
import { RecordsInfoFooter } from "../components/RecordsInfoFooter";
import { GuestFilters } from "../components/GuestFilters";
import { GuestTable } from "../components/GuestTable";
import { useGuestRecords } from "../hooks/useGuestRecords";
import { useExportGuestRecords } from "../hooks/useExportGuestRecords";
import type { GuestFilters as GuestFiltersType } from "../components/types";

const RecordsPage = () => {
  const [fechaInicio, setFechaInicio] = useState<Date | undefined>(undefined);
  const [fechaFin, setFechaFin] = useState<Date | undefined>(undefined);
  const [habitacion, setHabitacion] = useState("");
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState("all");
  const [page, setPage] = useState(0);

  const activeFilters = useMemo<GuestFiltersType>(
    () => ({
      fechaDesde: fechaInicio?.toISOString().split("T")[0],
      fechaHasta: fechaFin?.toISOString().split("T")[0],
      habitacion: habitacion || undefined,
      busqueda: busqueda || undefined,
      estado: estado === "all" ? undefined : estado,
    }),
    [fechaInicio, fechaFin, habitacion, busqueda, estado],
  );

  const { data, isLoading } = useGuestRecords({
    ...activeFilters,
    page,
    size: 15,
  });

  const { exportDocument } = useExportGuestRecords();

  const records = data?.content ?? [];
  const totalItems = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;

  const handleFilter = useCallback(() => {
    setPage(0);
  }, []);

  const handleClear = useCallback(() => {
    setFechaInicio(undefined);
    setFechaFin(undefined);
    setHabitacion("");
    setBusqueda("");
    setEstado("all");
    setPage(0);
  }, []);

  const handlePageChange = useCallback((newPage: number) => {
    setPage(newPage - 1);
  }, []);

  const handleViewDetail = useCallback(() => {
    // Placeholder - navegara a detalle en el futuro
  }, []);

  const handleExport = useCallback(
    async (format: "pdf" | "word") => {
      await exportDocument({
        ...activeFilters,
        format,
      });
    },
    [activeFilters, exportDocument],
  );

  const hasActiveFilters = !!(
    fechaInicio ||
    fechaFin ||
    habitacion ||
    busqueda ||
    (estado && estado !== "all")
  );

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <div className="space-y-4 rounded-xl border bg-card p-5 shadow-sm">
        <div>
          <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
            Registro de huéspedes
          </h2>
          <p className="text-sm text-muted-foreground">
            Consulta todas las estancias registradas en el sistema dentro del
            rango de fechas seleccionado.
          </p>
        </div>

        <GuestFilters
          fechaInicio={fechaInicio}
          fechaFin={fechaFin}
          habitacion={habitacion}
          busqueda={busqueda}
          estado={estado}
          onFechaInicioChange={setFechaInicio}
          onFechaFinChange={setFechaFin}
          onHabitacionChange={setHabitacion}
          onBusquedaChange={setBusqueda}
          onEstadoChange={setEstado}
          onFilter={handleFilter}
          onClear={handleClear}
          onExport={handleExport}
          hasActiveFilters={hasActiveFilters}
        />

        <GuestTable
          records={records}
          isLoading={isLoading}
          totalItems={totalItems}
          totalPages={totalPages}
          currentPage={page + 1}
          pageSize={15}
          onPageChange={handlePageChange}
          onViewDetail={handleViewDetail}
          onRefresh={handleFilter}
        />
      </div>

      <RecordsInfoFooter />
    </div>
  );
};

export default RecordsPage;
