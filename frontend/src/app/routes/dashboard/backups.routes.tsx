import { lazy } from "react";
import { Route } from "react-router-dom";
import { withSuspense } from "@/app/routes/helpers";
import ProtectedRoute from "@/app/auth/ProtectedRoute";
import ContentSkeleton from "@/components/ui/ContentSkeleton";

const BackupsPage = lazy(() => import("@/features/backups/pages/BackupsPage"));

/**
 * El backend restringe /api/backups/** a ROLE_ADMIN. Este gate evita que un RECEPCIONISTA con
 * la URL abra una pantalla que solo va a devolver 403 en cada llamada.
 */
export const backupsRoute = (
  <Route
    path="backups"
    element={
      <ProtectedRoute allowedRoles={["ADMIN"]}>
        {withSuspense(<BackupsPage />, <ContentSkeleton />)}
      </ProtectedRoute>
    }
  />
);
