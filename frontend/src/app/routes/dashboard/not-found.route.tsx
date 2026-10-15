import { lazy } from "react";
import { Route } from "react-router-dom";
import ContentSkeleton from "@/components/ui/ContentSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const DashboardNotFoundPage = lazy(
  () => import("@/features/dashboard/pages/DashboardNotFoundPage"),
);

export const notFoundRoute = (
  <Route
    path="*"
    element={withSuspense(<DashboardNotFoundPage />, <ContentSkeleton />)}
  />
);
