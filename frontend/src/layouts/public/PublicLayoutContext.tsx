import { createContext, useContext, useState, useCallback, useMemo, type ReactNode } from "react";
import { useAuth } from "@/app/auth/AuthContext";
import { useLogin } from "@/features/auth-client/hooks/useLogin";
import LoginModal from "@/features/home/components/LoginModal";
import CompleteProfileDialog from "@/features/home/components/CompleteProfileDialog";
import PQRSDialog from "@/features/pqrs/components/PQRSDialog";

interface PublicLayoutContextValue {
  openLogin: () => void;
  openCompleteProfile: () => void;
  openPQRSForm: () => void;
}

const PublicLayoutContext = createContext<PublicLayoutContextValue | null>(null);

export function usePublicLayout() {
  const ctx = useContext(PublicLayoutContext);
  if (!ctx) throw new Error("usePublicLayout must be used within <PublicLayout>");
  return ctx;
}

export function PublicLayoutProvider({ children }: { children: ReactNode }) {
  const [showLogin, setShowLogin] = useState(false);
  const [showCompleteProfile, setShowCompleteProfile] = useState(false);
  const [showPQRS, setShowPQRS] = useState(false);
  const { user } = useAuth();
  const { handleGoogleLogin, loading: loginLoading } = useLogin();

  // El modal de login es irrelevante con sesion activa: se deriva en render en
  // lugar de sincronizarlo con un efecto.
  const loginOpen = showLogin && !user;

  const openLogin = useCallback(() => setShowLogin(true), []);
  const openCompleteProfile = useCallback(() => setShowCompleteProfile(true), []);
  const openPQRSForm = useCallback(() => setShowPQRS(true), []);

  const value = useMemo(
    () => ({ openLogin, openCompleteProfile, openPQRSForm }),
    [openLogin, openCompleteProfile, openPQRSForm],
  );

  return (
    <PublicLayoutContext.Provider value={value}>
      <div className="force-light [color-scheme:light]">{children}</div>

      <LoginModal
        open={loginOpen}
        onClose={() => setShowLogin(false)}
        onGoogleLogin={() => handleGoogleLogin(false)}
        loading={loginLoading}
      />

      <CompleteProfileDialog
        open={showCompleteProfile}
        onOpenChange={setShowCompleteProfile}
      />

      <PQRSDialog
        open={showPQRS}
        onOpenChange={setShowPQRS}
      />
    </PublicLayoutContext.Provider>
  );
}
