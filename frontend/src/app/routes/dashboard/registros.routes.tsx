import { lazy } from "react";
import { Route } from "react-router-dom";
import ContentSkeleton from "@/components/ui/ContentSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const RecordsPage = lazy(
  () => import("@/features/registros/pages/RecordsPage"),
);

export const registrosRoute = (
  <Route
    path="registros"
    element={withSuspense(<RecordsPage />, <ContentSkeleton />)}
  />
);
