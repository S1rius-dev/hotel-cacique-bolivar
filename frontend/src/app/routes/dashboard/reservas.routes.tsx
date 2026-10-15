import { lazy } from "react";
import { Route } from "react-router-dom";
import FormSkeleton from "@/components/ui/FormSkeleton";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const ReservasPage = lazy(
  () => import("@/features/reservas/pages/ReservasTablePage"),
);
const NuevaReservaPage = lazy(
  () => import("@/features/reservas/pages/NuevaReservaPage"),
);
const ReservaDetailPage = lazy(
  () => import("@/features/reservas/pages/ReservaDetailPage"),
);
const EditarReservaPage = lazy(
  () => import("@/features/reservas/pages/EditarReservaPage"),
);
const ReservaPagosRedirect = lazy(
  () => import("@/features/reservas/pages/ReservaPagosRedirect"),
);

export const reservasRoutes = (
  <Route path="reservas">
    <Route
      index
      element={withSuspense(<ReservasPage />, <DataTableSkeleton />)}
    />
    <Route
      path="nueva"
      element={withSuspense(<NuevaReservaPage />, <FormSkeleton sections={2} />)}
    />
    <Route
      path=":id"
      element={withSuspense(<ReservaDetailPage />, <DetailSkeleton />)}
    />
    <Route
      path=":id/editar"
      element={withSuspense(<EditarReservaPage />, <FormSkeleton sections={2} />)}
    />
    <Route
      path=":id/pagos"
      element={withSuspense(<ReservaPagosRedirect />, <DetailSkeleton />)}
    />
  </Route>
);
