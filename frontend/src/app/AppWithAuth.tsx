import { useEffect } from "react";
import App from "@/App";
import { useAuth } from "@/app/auth/AuthContext";
import { setAuthContextRef } from "@/api/custom-instance";

/**
 * Puente entre el contexto de auth y el interceptor de Axios: le pasa el
 * contexto para que ante un 401 irrecuperable pueda limpiar la sesión.
 */
export default function AppWithAuth() {
  const auth = useAuth();

  useEffect(() => {
    setAuthContextRef(auth);
  }, [auth]);

  return <App />;
}
