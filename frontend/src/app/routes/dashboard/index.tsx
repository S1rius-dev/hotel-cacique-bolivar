import { lazy, Suspense } from "react";
import { Route, Navigate } from "react-router-dom";
import ProtectedRoute from "@/app/auth/ProtectedRoute";
import DashboardShellSkeleton from "@/components/ui/DashboardShellSkeleton";

import { homeRoute } from "@/app/routes/dashboard/home.route";
import { clientesRoutes } from "@/app/routes/dashboard/clientes.routes";
import { reservasRoutes } from "@/app/routes/dashboard/reservas.routes";
import { habitacionesRoutes } from "@/app/routes/dashboard/habitaciones.routes";
import { tiendaRoutes } from "@/app/routes/dashboard/tienda.routes";
import { ocupacionRoute } from "@/app/routes/dashboard/ocupacion.routes";
import { empleadosRoutes } from "@/app/routes/dashboard/empleados.routes";
import { cajaRoute } from "@/app/routes/dashboard/caja.routes";
import { pagosRoute } from "@/app/routes/dashboard/pagos.routes";
import { historialRoute } from "@/app/routes/dashboard/historial.routes";
import { pqrsRoute } from "@/app/routes/dashboard/pqrs.routes";
import { usuariosRoutes } from "@/app/routes/dashboard/usuarios.routes";
import { registrosRoute } from "@/app/routes/dashboard/registros.routes";
import { backupsRoute } from "@/app/routes/dashboard/backups.routes";
import { ajustesRoute } from "@/app/routes/dashboard/ajustes.routes";
import { notFoundRoute } from "@/app/routes/dashboard/not-found.route";

const DashboardLayout = lazy(
  () => import("@/layouts/dashboard/DashboardLayout"),
);

export const DashboardRoutes = (
  <Route
    path="/dashboard"
    element={
      <Suspense fallback={<DashboardShellSkeleton />}>
        <ProtectedRoute allowedRoles={["ADMIN", "RECEPCIONISTA"]}>
          <DashboardLayout />
        </ProtectedRoute>
      </Suspense>
    }
  >
    <Route index element={<Navigate to="home" replace />} />
    {homeRoute}
    {clientesRoutes}
    {reservasRoutes}
    {habitacionesRoutes}
    {tiendaRoutes}
    {ocupacionRoute}
    {empleadosRoutes}
    {cajaRoute}
    {pagosRoute}
    {historialRoute}
    {pqrsRoute}
    {usuariosRoutes}
    {registrosRoute}
    {backupsRoute}
    {ajustesRoute}
    {notFoundRoute}
  </Route>
);
