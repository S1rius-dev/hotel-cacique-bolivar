import { getAuthController } from "@/api/generated/auth-controller/auth-controller";
import { useAuth } from "@/app/auth/AuthContext";
import { toSessionUser } from "@/app/auth/session-user";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { extractErrorMessage } from "@/lib/extract-error";

const authApi = getAuthController();

export const useLogin = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGoogleLogin = (redirectTo?: string | false) => {
    const div = document.createElement("div");
    div.style.display = "none";
    document.body.appendChild(div);

    const callback = async (response: { credential?: string }) => {
      document.body.removeChild(div);
      if (!response.credential) return;

      setLoading(true);
      setError(null);
      try {
        await authApi.googleLogin({ idToken: response.credential });
        const me = await authApi.me();
        login(toSessionUser(me));
        if (redirectTo !== false) {
          navigate(redirectTo ?? "/", { replace: true });
        }
      } catch (err: unknown) {
        setError(extractErrorMessage(err, "Error al iniciar sesión con Google"));
      } finally {
        setLoading(false);
      }
    };

    const google = window.google?.accounts?.id;
    if (!google) {
      setError("Google Identity Services no disponible");
      document.body.removeChild(div);
      return;
    }

    google.initialize({
      client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
      callback,
    });

    google.renderButton(div, {
      type: "standard",
      shape: "pill",
      size: "large",
    });

    const btn = div.querySelector('[role="button"]');
    if (btn) {
      (btn as HTMLElement).click();
    } else {
      document.body.removeChild(div);
      setError("Error al iniciar sesión con Google");
    }
  };

  return { handleGoogleLogin, loading, error };
};
