import { parseMonto } from "./utils";
import { formatNumber } from "@/utils/format";

interface Props {
  modoCalculo: string;
  onModoCalculoChange: (modo: string) => void;
  valor: string;
  onValorChange: (valor: string) => void;
}

function AjusteConfigForm({ modoCalculo, onModoCalculoChange, valor, onValorChange }: Props) {
  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70 mb-3">
        Configuración
      </p>
      <div className="space-y-3">
        <div className="flex gap-2">
          {[
            { value: "FIJO", label: "Fijo", suffix: "$" },
            { value: "PORCENTUAL", label: "Porcentual", suffix: "%" },
          ].map((m) => (
            <button
              key={m.value}
              type="button"
              onClick={() => onModoCalculoChange(m.value)}
              className={`
                relative flex-1 px-3 py-2 rounded-lg text-xs font-medium transition-all duration-150
                ${modoCalculo === m.value
                  ? "bg-emerald-500/10 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 shadow-sm ring-1 ring-emerald-500/30"
                  : "bg-white/40 dark:bg-zinc-900/40 text-zinc-500 dark:text-zinc-400 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 hover:text-zinc-700 dark:hover:text-zinc-300 border border-zinc-200/50 dark:border-zinc-700/50"
                }
                active:scale-95
              `}
            >
              {m.label} <span className="font-mono">{m.suffix}</span>
            </button>
          ))}
        </div>

        <div className="relative">
          {modoCalculo !== "PORCENTUAL" && (
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-sm text-muted-foreground font-medium pointer-events-none">$</span>
          )}
          <input
            type="text"
            inputMode="numeric"
            value={valor ? formatNumber(parseMonto(valor)) : ""}
            onChange={(e) => {
              const num = parseMonto(e.target.value);
              onValorChange(num > 0 ? String(num) : "");
            }}
            placeholder="0"
            className={`
              w-full h-10 rounded-lg text-sm tabular-nums outline-none transition-all duration-150
              bg-white/60 dark:bg-zinc-900/60
              border border-zinc-200/60 dark:border-zinc-700/60
              hover:border-zinc-300 dark:hover:border-zinc-600
              focus:border-emerald-500/50 focus:ring-[3px] focus:ring-emerald-500/15
              ${modoCalculo !== "PORCENTUAL" ? "pl-8 pr-3" : "pr-8 pl-3"}
            `}
            autoComplete="off"
          />
          {modoCalculo === "PORCENTUAL" && (
            <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-sm text-muted-foreground pointer-events-none">%</span>
          )}
        </div>
      </div>
    </div>
  );
}

export default AjusteConfigForm;
