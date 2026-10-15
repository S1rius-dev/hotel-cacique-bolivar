import { lazy } from "react";
import { Route } from "react-router-dom";
import PageSkeleton from "@/components/ui/PageSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const StaffLoginPage = lazy(
  () => import("@/features/auth-staff/pages/StaffLoginPage"),
);

export const StaffRoutes = (
  <Route
    path="/hotel/login"
    element={withSuspense(<StaffLoginPage />, <PageSkeleton />)}
  />
);
