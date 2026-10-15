import { METODOS } from "./constants";

interface Props {
  value: string;
  onChange: (value: string) => void;
}

function MetodoPagoSelector({ value, onChange }: Props) {
  return (
    <div className="space-y-2">
      <label className="text-xs font-medium text-muted-foreground/80">Método de pago</label>
      <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-4">
        {METODOS.map((m) => {
          const Icon = m.icon;
          const logo = (m as unknown as { logo?: string }).logo;
          return (
            <button
              key={m.value}
              type="button"
              onClick={() => onChange(m.value)}
              className={`
                flex min-w-0 flex-col items-center justify-center gap-2 px-2 py-3 rounded-lg text-[13px] font-medium transition-all duration-150 active:scale-95
                ${value === m.value
                  ? "bg-emerald-500/10 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 shadow-sm ring-1 ring-emerald-500/30"
                  : "bg-white/40 dark:bg-zinc-900/40 text-zinc-500 dark:text-zinc-400 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 hover:text-zinc-700 dark:hover:text-zinc-300 border border-zinc-200/50 dark:border-zinc-700/50"
                }
              `}
            >
              {logo ? (
                <img
                  src={logo}
                  alt={m.label}
                  className={`object-contain shrink-0 ${m.value === "NEQUI" ? "w-6 h-6 scale-[1.15]" : "w-5 h-5"}`}
                />
              ) : (
                <Icon className="w-5 h-5 shrink-0" />
              )}
              <span className="text-xs leading-tight text-center break-words hyphens-auto">
                {m.label}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default MetodoPagoSelector;
