import { lazy } from "react";
import { Route } from "react-router-dom";
import PageSkeleton from "@/components/ui/PageSkeleton";
import { withSuspense } from "@/app/routes/helpers";
import { SEOHead } from "@/components/seo";

const Home = lazy(() => import("@/features/home/page/HomePage"));
const BookingPage = lazy(() => import("@/features/booking/pages/BookingPage"));
const PrivacyPage = lazy(() => import("@/features/legal/pages/PrivacyPage"));
const TermsPage = lazy(() => import("@/features/legal/pages/TermsPage"));
const HelpPage = lazy(() => import("@/features/legal/pages/HelpPage"));
const PagoPage = lazy(() => import("@/features/booking/pagos/pages/PagoPage"));
const PagoExitoPage = lazy(
  () => import("@/features/booking/pagos/pages/PagoExitoPage"),
);
const PublicLayoutWrapper = lazy(
  () => import("@/layouts/public/PublicLayoutWrapper"),
);
const ProfileModal = lazy(() => import("@/features/perfil/pages/ProfileModal"));
const ReservasModal = lazy(
  () => import("@/features/reservas-cliente/pages/ReservasModal"),
);

export const PublicRoutes = (
  <Route
    element={withSuspense(<PublicLayoutWrapper />, <PageSkeleton />)}
  >
    <Route path="/" element={withSuspense(<Home />, <PageSkeleton />)} />
    <Route
      path="/reservar"
      element={withSuspense(<BookingPage />, <PageSkeleton />)}
    />
    <Route
      path="/privacidad"
      element={withSuspense(<PrivacyPage />, <PageSkeleton />)}
    />
    <Route
      path="/terminos"
      element={withSuspense(<TermsPage />, <PageSkeleton />)}
    />
    <Route
      path="/ayuda"
      element={withSuspense(<HelpPage />, <PageSkeleton />)}
    />
    {/*
      Estas dos rutas montan la portada con un modal encima. Sin el SEOHead que
      va después de <Home />, heredarían su título, su canonical y su
      indexación, y Google vería dos URLs con el mismo contenido. El hideSeo
      es necesario por el orden de montaje: <Home /> es lazy y su SEOHead llega
      después, así que acabaría pisando el noindex de la ruta.
    */}
    <Route
      path="/mi-perfil"
      element={withSuspense(
        <>
          <Home hideSeo />
          <SEOHead
            title="Mi perfil"
            description="Gestiona tus datos y tus reservas"
            url="/mi-perfil"
            noindex
          />
          <ProfileModal />
        </>,
        <PageSkeleton />,
      )}
    />
    <Route
      path="/mis-reservas"
      element={withSuspense(
        <>
          <Home hideSeo />
          <SEOHead
            title="Mis reservas"
            description="Consulta y gestiona tus reservas"
            url="/mis-reservas"
            noindex
          />
          <ReservasModal />
        </>,
        <PageSkeleton />,
      )}
    />
    <Route
      path="/pago"
      element={withSuspense(<PagoPage />, <PageSkeleton />)}
    />
    <Route
      path="/pago/exito"
      element={withSuspense(<PagoExitoPage />, <PageSkeleton />)}
    />
  </Route>
);
