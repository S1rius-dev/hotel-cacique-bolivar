import { lazy } from "react";
import { Route } from "react-router-dom";
import PageSkeleton from "@/components/ui/PageSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const ProfileModal = lazy(() => import("@/features/perfil/pages/ProfileModal"));
const ReservasModal = lazy(
  () => import("@/features/reservas-cliente/pages/ReservasModal"),
);

export const ModalRoutes = (
  <>
    <Route
      path="/mi-perfil"
      element={withSuspense(<ProfileModal />, <PageSkeleton />)}
    />
    <Route
      path="/mis-reservas"
      element={withSuspense(<ReservasModal />, <PageSkeleton />)}
    />
  </>
);
