import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import logo from "@/assets/logo.png";
import { useRateLimit } from "@/lib/hooks/useRateLimit";

interface Props {
  onSubmit: (data: { email: string; password: string }) => void;
  loading?: boolean;
  error?: string | null;
  fieldErrors?: Record<string, string>;
  onForgotPassword?: () => void;
}

const LoginForm: React.FC<Props> = ({
  onSubmit,
  loading,
  error,
  fieldErrors = {},
  onForgotPassword,
}) => {
  const { isCoolingDown, secondsRemaining } = useRateLimit();
  const inputClasses = `w-full
      rounded-md
      bg-white/80 backdrop-blur-sm

      border border-rose-800
      text-gray-800 placeholder-gray-400

      transition-all duration-300 ease-in-out

      focus:outline-none
      focus:ring-2 focus:ring-rose-500/60
      focus:border-rose-800

      hover:border-rose-600

      shadow-sm hover:shadow-md focus:shadow-lg
    `;
  return (
    <div className="w-full flex justify-center items-center p-4">
      <div className="bg-card shadow-2xl rounded-xl p-8 w-full max-w-md flex flex-col items-center">
        {/* Logo */}
        <img src={logo} alt="logo" className="w-45 h-auto rounded-xl" />

        <h1 className="text-2xl font-heading font-semibold text-gray-800 dark:text-rose-800 mb-1">
          Hotel Cacique de Bolivar
        </h1>
        <span className="text-gray-500 text-sm mb-6">Accede a tu cuenta</span>

        {/* FORM */}
        <form
          className="flex flex-col gap-6 w-full"
          onSubmit={(e) => {
            e.preventDefault();
            const formData = new FormData(e.currentTarget);

            onSubmit({
              email: String(formData.get("email") || ""),
              password: String(formData.get("password") || ""),
            });
          }}>
          {/* EMAIL */}
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-rose-800">
              Email
            </label>

            <Input
              name="email"
              type="email"
              placeholder="correo@ejemplo.com"
              className={inputClasses}
              disabled={loading}
            />

            {fieldErrors.email && (
              <p className="text-xs text-red-600 mt-1">{fieldErrors.email}</p>
            )}
          </div>

          {/* PASSWORD */}
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-rose-800">
              Contraseña
            </label>

            <Input
              name="password"
              type="password"
              placeholder="••••••••"
              className={inputClasses}
              disabled={loading}
            />

            {fieldErrors.password && (
              <p className="text-xs text-red-600 mt-1">
                {fieldErrors.password}
              </p>
            )}
          </div>

          {/* BUTTON */}
          <Button
            type="submit"
            disabled={loading || isCoolingDown}
            className="
              w-auto min-w-37.5 h-10
              px-5 mx-auto

              flex items-center justify-center gap-2

              bg-rose-800 hover:bg-rose-700
              text-white font-semibold

              rounded-xl
              shadow-md hover:shadow-lg

              transition-all duration-200 ease-in-out

              hover:scale-[1.02] active:scale-[0.98]

              disabled:opacity-70 disabled:cursor-not-allowed
            ">
            {loading ? (
              <>
                <span className="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full" />
                Ingresando...
              </>
            ) : isCoolingDown ? (
              `Espera ${secondsRemaining}s`
            ) : (
              "Iniciar sesión"
            )}
          </Button>

          {error && <p className="text-sm text-red-600 text-center">{error}</p>}
        </form>

        <button
          type="button"
          onClick={onForgotPassword}
          className="mt-6 text-sm font-medium text-rose-600 hover:underline cursor-pointer bg-transparent border-none">
          ¿Olvidaste tu contraseña?
        </button>
      </div>
    </div>
  );
};

export default LoginForm;
