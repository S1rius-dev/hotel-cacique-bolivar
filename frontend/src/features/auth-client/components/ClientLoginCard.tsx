import { Card } from "@/components/ui/card";
import logo from "@/assets/logo.png";

import { FcGoogle } from "react-icons/fc";

type Props = {
  onGoogleLogin?: () => void;
  loading?: boolean;
};

export default function ClientLoginCard({
  onGoogleLogin,
  loading = false,
}: Props) {
  return (
    <div className="w-full flex justify-center items-center px-4">
      <Card
        className="
    w-full max-w-md
    overflow-hidden
    rounded-3xl
    border-0
    bg-white
    shadow-[0_20px_60px_rgba(0,0,0,0.12)]
  ">
        {/* CABECERA */}
        <div className="px-10 pb-8 text-center">
          <img
            src={logo}
            alt="Hotel Cacique de Bolivar"
            className="w-40 mx-auto mb-6"
          />

          <p className="text-xs tracking-[0.35em] uppercase text-amber-700 font-medium">
            Hotel Cacique de Bolivar
          </p>

          <div className="w-16 h-px bg-amber-500 mx-auto my-5" />

          <p className="text-sm text-black leading-relaxed">
            Realiza y gestiona tus reservas, consulta tu historial y disfruta
            una experiencia personalizada.
          </p>
        </div>

        {/* CONTENIDO */}
        <div className="flex flex-col items-center gap-3 px-10 pb-8">
          <button
            onClick={onGoogleLogin}
            disabled={loading}
            className="
              cursor-pointer

              h-12
              min-w-65
              px-8

              flex items-center justify-center gap-3

              rounded-xl
              border-2 border-rose-800/80

              bg-white

              text-sm font-medium text-gray-700

              hover:bg-rose-50/40
              hover:border-rose-900
              hover:text-rose-900

              active:scale-[0.98]

              transition-all duration-200

              disabled:opacity-60
              disabled:cursor-not-allowed
            ">
            <FcGoogle className="h-5 w-5 shrink-0" />
            <span>Continuar con Google</span>
          </button>

          <p className="mt-6 text-center text-xs text-gray-400">
            Al continuar aceptas nuestros términos y políticas.
          </p>
        </div>
      </Card>
    </div>
  );
}
