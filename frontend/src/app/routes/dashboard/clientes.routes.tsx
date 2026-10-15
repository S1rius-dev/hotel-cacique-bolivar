import { lazy } from "react";
import { Route } from "react-router-dom";
import FormSkeleton from "@/components/ui/FormSkeleton";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const ClientesPage = lazy(
  () => import("@/features/clientes/pages/ClientesTablePage"),
);
const ClienteFormPage = lazy(
  () => import("@/features/clientes/pages/ClienteFormPage"),
);
const ClienteDetailPage = lazy(
  () => import("@/features/clientes/pages/ClienteDetailPage"),
);
const ClienteEditPage = lazy(
  () => import("@/features/clientes/pages/ClienteEditPage"),
);

export const clientesRoutes = (
  <Route path="clientes">
    <Route
      index
      element={withSuspense(<ClientesPage />, <DataTableSkeleton />)}
    />
    <Route
      path="nuevo"
      element={withSuspense(<ClienteFormPage />, <FormSkeleton />)}
    />
    <Route path=":id">
      <Route
        index
        element={withSuspense(<ClienteDetailPage />, <DetailSkeleton />)}
      />
      <Route
        path="editar"
        element={withSuspense(<ClienteEditPage />, <FormSkeleton sections={2} />)}
      />
    </Route>
  </Route>
);
