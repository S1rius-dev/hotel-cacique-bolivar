import ClientLoginCard from "@/features/auth-client/components/ClientLoginCard";

interface Props {
  open: boolean;
  onClose: () => void;
  onGoogleLogin: () => void;
  loading: boolean;
}

export default function LoginModal({ open, onClose, onGoogleLogin, loading }: Props) {
  if (!open) return null;

  return (
    <div className="force-light fixed inset-0 z-50 flex items-center justify-center [color-scheme:light]">
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={onClose}
      />

      <div
        className="
          relative z-10
          w-full max-w-2xl
          scale-105
        "
        onClick={(e) => e.stopPropagation()}>
        <ClientLoginCard
          onGoogleLogin={onGoogleLogin}
          loading={loading}
        />
      </div>
    </div>
  );
}
