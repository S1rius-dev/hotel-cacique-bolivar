import { useState } from "react";
import Login from "@/features/auth-staff/components/StaffLoginCard";
import Navbar from "@/components/layout/Navbar";
import fondoLogin from "@/assets/hotel-fachada.webp";
import { useLogin } from "@/features/auth-staff/hooks/useLogin";
import RecoverPasswordCard from "@/features/auth-staff/components/RecoverPasswordCard";
import { SEOHead } from "@/components/seo";

const NAVBAR_HEIGHT = "h-16";

const LoginPage: React.FC = () => {
  const { submit, loading, error, fieldErrors } = useLogin();
  const [view, setView] = useState<"login" | "recover">("login");

  return (
    <>
      {/* Panel interno: sin esto Google puede descubrirlo por enlaces internos. */}
      <SEOHead
        title="Acceso del personal"
        description="Ingreso restringido al personal del hotel"
        url="/hotel/login"
        noindex
      />
      <div
      className="h-screen w-full bg-cover bg-center overflow-hidden"
      style={{ backgroundImage: `url(${fondoLogin})` }}>
      <div className="h-full bg-black/60 flex flex-col">
        {/* NAVBAR */}
        <div className={`w-full ${NAVBAR_HEIGHT} shrink-0`}>
          <Navbar
            links={[]}
            className="text-white"
            linkClassName="hover:bg-white/10"
          />
        </div>

        {/* CONTENIDO */}
        <main className="flex-1 flex items-center justify-center px-4">
          {view === "login" ? (
            <Login
              onSubmit={submit}
              loading={loading}
              error={error}
              fieldErrors={fieldErrors}
              onForgotPassword={() => setView("recover")}
            />
          ) : (
            <RecoverPasswordCard onBackToLogin={() => setView("login")} />
          )}
        </main>
      </div>
    </div>
    </>
  );
};

export default LoginPage;
