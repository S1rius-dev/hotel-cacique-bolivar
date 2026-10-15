import { X, AlertTriangle } from "lucide-react";

interface Props {
  onCompleteProfile: () => void;
  onDismiss: () => void;
}

export default function OnboardingBanner({
  onCompleteProfile,
  onDismiss,
}: Props) {
  return (
    <div
      className="
        relative
        bg-gradient-to-r from-amber-600 via-rose-600 to-amber-600
        animate-[gradientShift_6s_ease_infinite]
      "
      style={{
        backgroundSize: "200% 100%",
      }}
    >
      <style>{`
        @keyframes gradientShift {
          0%, 100% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
        }
      `}</style>

      <div className="mx-auto max-w-[90rem] px-4 md:px-8 py-3 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3 min-w-0">
          <AlertTriangle className="size-4 text-white/80 shrink-0" />
          <p className="text-sm text-white/90 font-medium truncate">
            Completa tu perfil para acceder a tus reservas y dejar reseñas
          </p>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            type="button"
            onClick={onCompleteProfile}
            className="
              text-xs font-semibold uppercase tracking-wider
              px-4 py-1.5 rounded-full
              bg-white/20 hover:bg-white/30
              text-white
              transition-all duration-200
              cursor-pointer
              whitespace-nowrap
            "
          >
            Completar perfil
          </button>
          <button
            type="button"
            onClick={onDismiss}
            className="
              size-7 rounded-full
              flex items-center justify-center
              text-white/60 hover:text-white
              hover:bg-white/10
              transition-colors
              cursor-pointer
            "
          >
            <X className="size-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
