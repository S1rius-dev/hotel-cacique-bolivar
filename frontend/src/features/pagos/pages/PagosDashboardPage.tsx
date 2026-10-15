import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { MOCK_PAGOS } from "../mocks/mockPagos";
import PagosMetrics from "../components/dashboard/PagosMetrics";
import PagosFilters from "../components/dashboard/PagosFilters";
import PagoRow from "../components/dashboard/PagoRow";
import { toPagoDashboard } from "../utils/mapPago";
import type { PagoDashboard } from "../types/dashboard.types";
import {
  usePagosDashboard,
  useMetricasPagos,
} from "../hooks/usePagosDashboard";
import { useMetricas as useTiendaMetricas } from "@/features/tienda/hooks/useTienda";
import { useTurnoActivo } from "@/features/turnos";
import { usePaymentGatewayConfig } from "@/features/payments/hooks/usePaymentGatewayConfig";
import {
  CANAL_EFECTIVO,
  CANAL_SIN_FILTRO,
  canalDeGateway,
} from "@/features/payments/constants/gateway";

export default function PagosDashboardPage() {
  const [search, setSearch] = useState("");
  const [canal, setCanal] = useState(CANAL_SIN_FILTRO);
  const [estado, setEstado] = useState(CANAL_SIN_FILTRO);
  const [page, setPage] = useState(0);
  const size = 20;
  const { gateway, isLoading: gatewayLoading } = usePaymentGatewayConfig();
  const gatewayDisplayName = gateway?.displayName ?? (gatewayLoading ? "Cargando..." : "Pasarela");
  const canalOnline = canalDeGateway(gateway?.gateway);
  // Si la pasarela activa cambia, el canal online filtrado queda obsoleto y sin
  // coincidencias. Se resuelve en render en vez de en un effect para no resetear
  // estado ni provocar renders en cascada.
  const canalSeleccionado =
    canal === CANAL_SIN_FILTRO || canal === CANAL_EFECTIVO || canal === canalOnline
      ? canal
      : CANAL_SIN_FILTRO;

  const { data: pageData, isLoading } = usePagosDashboard({
    q: search,
    canal: canalSeleccionado,
    estado,
    page,
    size,
  });
  const { data: metricasData } = useMetricasPagos();
  const { data: tiendaMetricas } = useTiendaMetricas();
  const { data: turnoRecepcion } = useTurnoActivo({ caja: "RECEPCION" });
  const { data: turnoTienda } = useTurnoActivo({ caja: "TIENDA" });

  // Al cambiar un filtro se vuelve a la primera pagina. Se ajusta durante el
  // render (patron oficial de React) en vez de en un efecto, que provocaria un
  // render extra con la pagina anterior.
  const filterKey = `${search}|${canalSeleccionado}|${estado}`;
  const [lastFilterKey, setLastFilterKey] = useState(filterKey);
  if (filterKey !== lastFilterKey) {
    setLastFilterKey(filterKey);
    setPage(0);
  }

  // Fallback a mock si la API no está disponible (dev sin backend)
  const pagos: PagoDashboard[] = useMemo(
    () =>
      pageData?.content?.map(toPagoDashboard) ??
      MOCK_PAGOS.filter((p) => {
        const q = search.trim().toLowerCase();
        const matchSearch =
          !q ||
          p.clienteNombre.toLowerCase().includes(q) ||
          (p.wompiTransactionId?.toLowerCase().includes(q) ?? false) ||
          (p.numeroRecibo?.toLowerCase().includes(q) ?? false) ||
          p.habitacion.toLowerCase().includes(q);        const matchCanal =
          canalSeleccionado === CANAL_SIN_FILTRO || p.canal === canalSeleccionado;
        const matchEstado = estado === CANAL_SIN_FILTRO || p.estado === estado;
        return matchSearch && matchCanal && matchEstado;
      }).slice(page * size, page * size + size),
    [pageData, page, size, search, canalSeleccionado, estado],
  );

  const totalElements = pageData?.totalElements ?? MOCK_PAGOS.length;
  const totalPages =
    pageData?.totalPages ?? Math.ceil(MOCK_PAGOS.length / size);

  const metrics = useMemo(() => {
    // helper para separar efectivo en recepción / tienda
    const buildSeparated = (efectivoMonto: number, efectivoCount: number) => {
      // prioriza datos reales de tienda/turnos si existen
      const tiendaMontoFromApi =
        tiendaMetricas?.vendidosHoyMonto ?? turnoTienda?.totalIngresosEfectivo ?? null;
      const tiendaCountFromApi =
        tiendaMetricas?.vendidosHoyCount ?? turnoTienda?.movimientos?.length ?? null;

      let tiendaMonto: number;
      let tiendaCount: number;
      let recepcionMonto: number;
      let recepcionCount: number;

      if (tiendaMontoFromApi != null && tiendaMontoFromApi >= 0) {
        tiendaMonto = Number(tiendaMontoFromApi) || 0;
        tiendaCount = Number(tiendaCountFromApi) || 0;
        // si el total efectivo es menor que tienda (datos desfasados), usa tienda como tienda y 0 recepción
        if (efectivoMonto >= tiendaMonto) {
          recepcionMonto = efectivoMonto - tiendaMonto;
          recepcionCount = Math.max(0, efectivoCount - tiendaCount);
        } else if (turnoRecepcion?.totalIngresosEfectivo != null) {
          recepcionMonto = Number(turnoRecepcion.totalIngresosEfectivo) || 0;
          recepcionCount =
            Number(turnoRecepcion?.movimientos?.length) ||
            Math.max(0, efectivoCount - tiendaCount);
          // recalcula tienda si hace falta
          tiendaMonto = Math.max(0, efectivoMonto - recepcionMonto);
        } else {
          recepcionMonto = 0;
          recepcionCount = 0;
        }
      } else if (
        turnoRecepcion?.totalIngresosEfectivo != null ||
        turnoTienda?.totalIngresosEfectivo != null
      ) {
        recepcionMonto = Number(turnoRecepcion?.totalIngresosEfectivo) || 0;
        tiendaMonto = Number(turnoTienda?.totalIngresosEfectivo) || 0;
        recepcionCount = Number(turnoRecepcion?.movimientos?.length) || 0;
        tiendaCount = Number(turnoTienda?.movimientos?.length) || 0;
        // si alguno faltó, completa con diferencia
        if (!recepcionCount && !tiendaCount) {
          recepcionCount = Math.ceil(efectivoCount * 0.6);
          tiendaCount = efectivoCount - recepcionCount;
        }
      } else {
        // fallback sin API de turnos/tienda: split 60/40
        tiendaMonto = Math.round(efectivoMonto * 0.35);
        recepcionMonto = efectivoMonto - tiendaMonto;
        tiendaCount = Math.round(efectivoCount * 0.35);
        recepcionCount = efectivoCount - tiendaCount;
      }

      return {
        efectivoRecepcion: { monto: recepcionMonto, count: recepcionCount },
        efectivoTienda: { monto: tiendaMonto, count: tiendaCount },
      };
    };

    if (metricasData) {
      const efectivoMonto = Number(metricasData.efectivoMonto ?? 0);
      const efectivoCount = Number(metricasData.efectivoCount ?? 0);
      const separated = buildSeparated(efectivoMonto, efectivoCount);
      return {
        recaudadoHoy: Number(metricasData.recaudadoHoy ?? 0),
        wompi: {
          monto: Number(metricasData.wompiMonto ?? 0),
          count: Number(metricasData.wompiCount ?? 0),
        },
        efectivo: {
          monto: efectivoMonto,
          count: efectivoCount,
        },
        ...separated,
        pendientes: Number(metricasData.pendientes ?? 0),
      };
    }
    // fallback mock
    const hoy = new Date().toDateString();
    const hoyPagos = MOCK_PAGOS.filter(
      (p) =>
        new Date(p.createdAt).toDateString() === hoy && p.estado === "aprobada",
    );
    const recaudadoHoy = hoyPagos.reduce((a, b) => a + b.monto, 0);
    const wompi = MOCK_PAGOS.filter(
      (p) => p.canal === "wompi" && p.estado === "aprobada",
    );
    const efectivo = MOCK_PAGOS.filter(
      (p) => p.canal === "efectivo" && p.estado === "aprobada",
    );
    const efectivoMonto = efectivo.reduce((a, b) => a + b.monto, 0);
    const efectivoCount = efectivo.length;
    const tiendaMonto = Number(tiendaMetricas?.vendidosHoyMonto ?? 0);
    const tiendaCount = Number(tiendaMetricas?.vendidosHoyCount ?? 0);
    const separated =
      tiendaMonto > 0 || tiendaCount > 0
        ? {
            efectivoRecepcion: {
              monto: Math.max(0, efectivoMonto - tiendaMonto),
              count: Math.max(0, efectivoCount - tiendaCount),
            },
            efectivoTienda: { monto: tiendaMonto, count: tiendaCount },
          }
        : buildSeparated(efectivoMonto, efectivoCount);

    const pendientes = MOCK_PAGOS.filter(
      (p) => p.estado === "pendiente",
    ).length;
    return {
      recaudadoHoy,
      wompi: {
        monto: wompi.reduce((a, b) => a + b.monto, 0),
        count: wompi.length,
      },
      efectivo: {
        monto: efectivoMonto,
        count: efectivoCount,
      },
      ...separated,
      pendientes,
    };
  }, [metricasData, tiendaMetricas, turnoRecepcion, turnoTienda]);

  return (
    <div className="min-h-full bg-card p-4 md:p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-heading font-semibold text-[#f2f2f0]">
            Pagos
          </h1>
          <p className="text-sm text-[#8b8f9c] mt-1">
            Gestión de recaudo · {gatewayDisplayName} y caja
          </p>
        </div>
      </div>

      <PagosMetrics metrics={metrics} gatewayDisplayName={gatewayDisplayName} />

      <PagosFilters
        search={search}
        onSearchChange={setSearch}
        canal={canalSeleccionado}
        onCanalChange={setCanal}
        estado={estado}
        onEstadoChange={setEstado}
        canalOnline={canalOnline}
        canalOnlineLabel={gatewayDisplayName}
      />

      {/* Lista de transacciones - NO tabla */}
      <div className="space-y-3">
        <div className="flex items-center justify-between px-1">
          <p className="text-xs tracking-widest uppercase font-medium text-muted-foreground">
            Transacciones · {totalElements}
          </p>
          <span className="text-xs text-muted-foreground hidden sm:inline">
            Toca para ver trazabilidad
          </span>
        </div>

        {isLoading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="rounded-xl border bg-card border-border p-6 animate-pulse h-20"
              />
            ))}
          </div>
        ) : pagos.length === 0 ? (
          <div className="rounded-xl border border-dashed bg-card border-border p-10 text-center">
            <p className="text-sm text-muted-foreground">
              No se encontraron pagos con esos filtros
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {pagos.map((p) => (
              <PagoRow
                key={p.id}
                pago={p}
                gateway={gateway?.gateway}
                gatewayDisplayName={gatewayDisplayName}
              />
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="flex items-center justify-between pt-2">
            <p className="text-xs text-muted-foreground">
              Página {page + 1} de {totalPages} · {totalElements} registros
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="border-border">
                <ChevronLeft className="size-4" /> Anterior
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="border-border">
                Siguiente <ChevronRight className="size-4" />
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
