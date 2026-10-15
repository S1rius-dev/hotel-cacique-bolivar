import { useMutation } from "@tanstack/react-query";
import type { ApiError } from "@/api/custom-instance";
import { getAuthController } from "@/api/generated/auth-controller/auth-controller";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/app/auth/AuthContext";
import { toSessionUser } from "@/app/auth/session-user";
import { extractErrorMessage, extractFieldErrors } from "@/lib/extract-error";

const authApi = getAuthController();

export const useLogin = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const mutation = useMutation<
    unknown,
    ApiError,
    { email: string; password: string }
  >({
    mutationFn: (data) => authApi.login(data),

    onSuccess: async () => {
      const me = await authApi.me();
      login(toSessionUser(me));
      navigate("/dashboard", { replace: true });
    },
  });

  return {
    submit: mutation.mutate,
    loading: mutation.isPending,
    error: mutation.error
      ? extractErrorMessage(mutation.error, "Error al iniciar sesión")
      : undefined,
    fieldErrors: extractFieldErrors(mutation.error),
  };
};
