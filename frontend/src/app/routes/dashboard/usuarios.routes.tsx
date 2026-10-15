import { lazy } from "react";
import { Route } from "react-router-dom";
import FormSkeleton from "@/components/ui/FormSkeleton";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const UsuariosPage = lazy(() => import("@/features/users/pages/UserTablePage"));
const UserDetailPage = lazy(
  () => import("@/features/users/pages/UserDetailPage"),
);
const UserEditPage = lazy(() => import("@/features/users/pages/UserEditPage"));
const UserFormPage = lazy(() => import("@/features/users/pages/UserFormPage"));

export const usuariosRoutes = (
  <Route path="usuarios">
    <Route
      index
      element={withSuspense(<UsuariosPage />, <DataTableSkeleton />)}
    />
    <Route
      path="nuevo"
      element={withSuspense(<UserFormPage />, <FormSkeleton />)}
    />
    <Route path=":id">
      <Route
        index
        element={withSuspense(<UserDetailPage />, <DetailSkeleton />)}
      />
      <Route
        path="editar"
        element={withSuspense(<UserEditPage />, <FormSkeleton />)}
      />
    </Route>
  </Route>
);
