import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import logo from "@/assets/logo.png";

import { FcGoogle } from "react-icons/fc";
import { FaFacebookF } from "react-icons/fa";

type Props = {
  onGoogleRegister?: () => void;
  onFacebookRegister?: () => void;
  onLoginClick?: () => void; // 👈 volver a login
  loading?: boolean;
};

export default function ClientRegisterCard({
  onGoogleRegister,
  onFacebookRegister,
  onLoginClick,
  loading = false,
}: Props) {
  return (
    <div className="w-full flex justify-center items-center px-4">
      <Card
        className="
          w-full max-w-md
          p-10
          rounded-3xl
          bg-white/95 backdrop-blur-xl
          shadow-[0_25px_80px_rgba(0,0,0,0.25)]
          flex flex-col
        ">
        {/* HEADER */}
        <div className="flex flex-col items-center mb-8">
          <img
            src={logo}
            alt="logo"
            className="w-28 h-auto rounded-2xl mb-4 shadow-sm"
          />

          <h1 className="text-xl font-semibold text-gray-800 text-center">
            Crear cuenta
          </h1>

          <p className="text-gray-500 text-sm mt-1 text-center">
            Regístrate rápido y empieza a reservar
          </p>
        </div>

        {/* SOCIAL REGISTER */}
        <div className="flex flex-col gap-3">
          {/* GOOGLE */}
          <Button
            onClick={onGoogleRegister}
            variant="outline"
            className="
              h-12
              flex items-center justify-center gap-3
              rounded-xl border border-gray-300
              bg-white
              hover:bg-gray-50
              hover:shadow-md
              transition-all
            "
            disabled={loading}>
            <FcGoogle className="h-6 w-6" />
            <span className="font-medium text-gray-700 text-sm">
              Registrarse con Google
            </span>
          </Button>

          {/* FACEBOOK */}
          <Button
            onClick={onFacebookRegister}
            className="
              h-12
              flex items-center justify-center gap-3
              rounded-xl
              bg-[#1877F2] hover:bg-[#166FE5]
              text-white font-medium text-sm
              shadow-sm hover:shadow-md
              transition-all
            "
            disabled={loading}>
            <FaFacebookF className="h-5 w-5" />
            Registrarse con Facebook
          </Button>
        </div>

        {/* DIVIDER */}
        <div className="flex items-center gap-4 my-6">
          <div className="flex-1 h-px bg-gray-200" />
          <span className="text-xs text-gray-400">o</span>
          <div className="flex-1 h-px bg-gray-200" />
        </div>

        {/* LOGIN CTA */}
        <div className="text-center">
          <p className="text-sm text-gray-500">¿Ya tienes cuenta?</p>

          <button
            onClick={onLoginClick}
            className="
              mt-2
              text-sm font-semibold
              text-rose-800
              hover:text-rose-700
              transition-colors
            ">
            Iniciar sesión
          </button>
        </div>
      </Card>
    </div>
  );
}
