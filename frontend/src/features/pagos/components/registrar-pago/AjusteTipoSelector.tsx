import { TIPOS_AJUSTE, TIPO_GLOW, TIPO_BG, TIPO_ICON_BG } from "./constants";

interface Props {
  value: string;
  onChange: (value: string) => void;
  compact?: boolean;
}

function AjusteTipoSelector({ value, onChange, compact }: Props) {
  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70 mb-2">
        Tipo de ajuste
      </p>
      <div className="grid grid-cols-3 gap-2">
        {TIPOS_AJUSTE.map((t) => {
          const Icon = t.icon;
          const isActive = value === t.value;
          return (
            <button
              key={t.value}
              type="button"
              onClick={() => onChange(t.value)}
              className={`
                relative flex flex-col items-center gap-1.5 px-2 ${compact ? "py-2" : "py-3.5"} rounded-xl text-center
                transition-all duration-200 ease-out
                ${isActive
                  ? `${TIPO_BG[t.color]} ${TIPO_GLOW[t.color]} scale-[1.02]`
                  : "bg-white/40 dark:bg-zinc-900/40 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 hover:scale-[1.01] shadow-sm"
                }
                border ${isActive ? "border-transparent" : "border-zinc-200/50 dark:border-zinc-700/50 hover:border-zinc-300 dark:hover:border-zinc-600"}
              `}
            >
              <span className={`${compact ? "p-1.5" : "p-2"} rounded-xl transition-all ${isActive ? TIPO_ICON_BG[t.color] : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400"}`}>
                <Icon className={`${compact ? "w-3.5 h-3.5" : "w-4 h-4"}`} />
              </span>
              <div>
                <p className={`${compact ? "text-xs" : "text-sm"} font-semibold leading-tight ${isActive ? "text-zinc-900 dark:text-zinc-100" : "text-zinc-700 dark:text-zinc-300"}`}>
                  {t.label}
                </p>
                {!compact && (
                  <p className="text-[10px] text-muted-foreground/70 mt-0.5 leading-tight">{t.subtitle}</p>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default AjusteTipoSelector;
