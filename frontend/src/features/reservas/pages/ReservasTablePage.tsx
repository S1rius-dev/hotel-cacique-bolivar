import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useReservas } from "@/features/reservas/hooks/useReservas";
import type { ReservaFilters } from "@/features/reservas/hooks/useReservas";
import { useReservasStats } from "@/features/reservas/hooks/useReservasStats";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { reservaColumn } from "@/features/reservas/components/reservaColumn";
import { ReservaFilters as ReservaFiltersComponent } from "@/features/reservas/components/ReservaFilters";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { CalendarCheck } from "lucide-react";

const DEFAULT_SIZE = 15;

function parseDateParam(value: string | null): Date | undefined {
  if (!value) return undefined;
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [y, m, d] = value.split("-").map(Number);
    const parsed = new Date(y, m - 1, d);
    return Number.isNaN(parsed.getTime()) ? undefined : parsed;
  }
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? undefined : d;
}

const ReservaTablePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const size = parseInt(searchParams.get("size") ?? String(DEFAULT_SIZE), 10);

  const [codigo, setCodigo] = useState(searchParams.get("codigo") ?? "");
  const [cliente, setCliente] = useState(searchParams.get("cliente") ?? "");
  const [estado, setEstado] = useState(searchParams.get("estado") ?? "");
  const [origen, setOrigen] = useState(searchParams.get("origen") ?? "");
  const [checkInDesde, setCheckInDesde] = useState<Date | undefined>(parseDateParam(searchParams.get("checkInDesde")));
  const [checkInHasta, setCheckInHasta] = useState<Date | undefined>(parseDateParam(searchParams.get("checkInHasta")));
  const [checkOutDesde, setCheckOutDesde] = useState<Date | undefined>(parseDateParam(searchParams.get("checkOutDesde")));
  const [checkOutHasta, setCheckOutHasta] = useState<Date | undefined>(parseDateParam(searchParams.get("checkOutHasta")));

  const debouncedCodigo = useDebounce(codigo, 300);
  const debouncedCliente = useDebounce(cliente, 300);

  const toDateStr = (d: Date | undefined) => d?.toISOString().slice(0, 10) ?? "";

  useEffect(() => {
    const uCodigo = searchParams.get("codigo") ?? "";
    const uCliente = searchParams.get("cliente") ?? "";
    const uEstado = searchParams.get("estado") ?? "";
    const uOrigen = searchParams.get("origen") ?? "";
    const uCheckInDesde = searchParams.get("checkInDesde") ?? "";
    const uCheckInHasta = searchParams.get("checkInHasta") ?? "";
    const uCheckOutDesde = searchParams.get("checkOutDesde") ?? "";
    const uCheckOutHasta = searchParams.get("checkOutHasta") ?? "";

    if (
      debouncedCodigo === uCodigo &&
      debouncedCliente === uCliente &&
      estado === uEstado &&
      origen === uOrigen &&
      toDateStr(checkInDesde) === uCheckInDesde &&
      toDateStr(checkInHasta) === uCheckInHasta &&
      toDateStr(checkOutDesde) === uCheckOutDesde &&
      toDateStr(checkOutHasta) === uCheckOutHasta
    )
      return;

    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (debouncedCodigo) next.set("codigo", debouncedCodigo);
      else next.delete("codigo");
      if (debouncedCliente) next.set("cliente", debouncedCliente);
      else next.delete("cliente");
      if (estado) next.set("estado", estado);
      else next.delete("estado");
      if (origen) next.set("origen", origen);
      else next.delete("origen");
      if (checkInDesde) next.set("checkInDesde", toDateStr(checkInDesde));
      else next.delete("checkInDesde");
      if (checkInHasta) next.set("checkInHasta", toDateStr(checkInHasta));
      else next.delete("checkInHasta");
      if (checkOutDesde) next.set("checkOutDesde", toDateStr(checkOutDesde));
      else next.delete("checkOutDesde");
      if (checkOutHasta) next.set("checkOutHasta", toDateStr(checkOutHasta));
      else next.delete("checkOutHasta");
      next.set("page", "0");
      return next;
    });
  }, [debouncedCodigo, debouncedCliente, estado, origen, checkInDesde, checkInHasta, checkOutDesde, checkOutHasta, searchParams, setSearchParams]);

  const filterParams: ReservaFilters = {
    codigo: debouncedCodigo || undefined,
    cliente: debouncedCliente || undefined,
    estado: estado || undefined,
    origen: origen || undefined,
    checkInDesde: toDateStr(checkInDesde) || undefined,
    checkInHasta: toDateStr(checkInHasta) || undefined,
    checkOutDesde: toDateStr(checkOutDesde) || undefined,
    checkOutHasta: toDateStr(checkOutHasta) || undefined,
  };

  const { data, isLoading } = useReservas(page, size, filterParams);

  const {
    total: totalStats,
    pendientes,
    confirmadas,
    enCurso,
    finalizadas,
    canceladas,
    noShow,
    isLoading: statsLoading,
  } = useReservasStats();

  const reservas = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;
  const currentPage = data?.page ?? 0;

  const hayFiltros = !!(codigo || cliente || estado || origen || checkInDesde || checkInHasta || checkOutDesde || checkOutHasta);

  const limpiarFiltros = () => {
    setCodigo("");
    setCliente("");
    setEstado("");
    setOrigen("");
    setCheckInDesde(undefined);
    setCheckInHasta(undefined);
    setCheckOutDesde(undefined);
    setCheckOutHasta(undefined);
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.delete("codigo");
      next.delete("cliente");
      next.delete("estado");
      next.delete("origen");
      next.delete("checkInDesde");
      next.delete("checkInHasta");
      next.delete("checkOutDesde");
      next.delete("checkOutHasta");
      next.set("page", "0");
      next.set("size", String(pageSize || DEFAULT_SIZE));
      return next;
    });
  };

  const filterBar = (
    <ReservaFiltersComponent
      codigo={codigo}
      onCodigoChange={setCodigo}
      cliente={cliente}
      onClienteChange={setCliente}
      estado={estado}
      onEstadoChange={setEstado}
      origen={origen}
      onOrigenChange={setOrigen}
      checkInDesde={checkInDesde}
      onCheckInDesdeChange={setCheckInDesde}
      checkInHasta={checkInHasta}
      onCheckInHastaChange={setCheckInHasta}
      checkOutDesde={checkOutDesde}
      onCheckOutDesdeChange={setCheckOutDesde}
      checkOutHasta={checkOutHasta}
      onCheckOutHastaChange={setCheckOutHasta}
      onClear={limpiarFiltros}
      hasActiveFilters={hayFiltros}
    />
  );

  return (
    <ManagementTablePage
      title="Gestión de Reservas"
      titleIcon={<CalendarCheck className="w-6 h-6 text-rose-600" />}
      subtitle="Gestiona reservas, estadías y movimientos"
      stats={
        isLoading || statsLoading
          ? undefined
          : [
              { label: "total", value: totalStats.toLocaleString("es-CO") },
              { label: "pendientes", value: pendientes.toLocaleString("es-CO") },
              { label: "confirmadas", value: confirmadas.toLocaleString("es-CO") },
              { label: "en curso", value: enCurso.toLocaleString("es-CO") },
              { label: "finalizadas", value: finalizadas.toLocaleString("es-CO") },
              { label: "canceladas", value: canceladas.toLocaleString("es-CO") },
              { label: "no llegó", value: noShow.toLocaleString("es-CO") },
            ]
      }
      data={reservas}
      columns={reservaColumn}
      loading={isLoading}
      createLabel="Realizar Reserva"
      onCreate={() => navigate("nueva")}
      onRowClick={(row) => navigate(String(row.id))}
      currentPage={currentPage + 1}
      totalPages={totalPages}
      totalItems={totalElements}
      pageSize={pageSize}
      onPageChange={(p) =>
        setSearchParams((prev) => {
          const next = new URLSearchParams(prev);
          next.set("page", String(p - 1));
          next.set("size", String(pageSize || DEFAULT_SIZE));
          return next;
        })
      }
      filterBar={filterBar}
    />
  );
};

export default ReservaTablePage;
