interface Props {
  scrolled: boolean;
  onClick: () => void;
}

export default function LoginButton({ scrolled, onClick }: Props) {
  return (
    <button
      onClick={onClick}
      className={`
        px-4 py-1.5 rounded-full font-medium text-sm cursor-pointer
        border transition-all duration-200 hover:scale-105
        ${
          scrolled
            ? "border-rose-200 text-rose-600 hover:bg-rose-50"
            : "border-white/30 text-white hover:bg-white/10"
        }
      `}>
      iniciar sesión →
    </button>
  );
}
