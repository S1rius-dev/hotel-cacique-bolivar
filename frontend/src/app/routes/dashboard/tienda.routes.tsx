import { lazy } from "react";
import { Route } from "react-router-dom";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const TiendaPage = lazy(() => import("@/features/tienda/pages/TiendaPage"));

export const tiendaRoutes = (
  <Route path="tienda">
    <Route index element={withSuspense(<TiendaPage />, <DataTableSkeleton />)} />
  </Route>
);
