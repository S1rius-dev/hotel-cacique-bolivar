import { X, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import AjusteTipoSelector from "./AjusteTipoSelector";
import AjusteConfigForm from "./AjusteConfigForm";
import { PRESETS } from "./constants";

interface Props {
  onClose: () => void;
  tipoAjuste: string;
  onTipoAjusteChange: (tipo: string) => void;
  modoCalculo: string;
  onModoCalculoChange: (modo: string) => void;
  valor: string;
  onValorChange: (valor: string) => void;
  concepto: string;
  onConceptoChange: (concepto: string) => void;
  isValid: boolean;
  isPending: boolean;
  onAdd: () => void;
  onCancel: () => void;
}

function AjusteSidePanel({
  onClose,
  tipoAjuste,
  onTipoAjusteChange,
  modoCalculo,
  onModoCalculoChange,
  valor,
  onValorChange,
  concepto,
  onConceptoChange,
  isValid,
  isPending,
  onAdd,
  onCancel,
}: Props) {
  return (
    <div className="flex flex-col h-full min-h-0 rounded-xl bg-zinc-50/50 dark:bg-zinc-900/30 shadow-sm border border-zinc-200/50 dark:border-zinc-800/50 backdrop-blur-sm">
      <div className="flex items-center justify-between px-4 py-3 border-b border-zinc-200/40 dark:border-zinc-800/40 shrink-0">
        <span className="text-sm font-semibold">Añadir ajuste</span>
        <button
          type="button"
          onClick={onClose}
          className="p-1 rounded-md text-muted-foreground hover:text-foreground hover:bg-zinc-200/50 dark:hover:bg-zinc-800/50 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        <AjusteTipoSelector
          compact
          value={tipoAjuste}
          onChange={(t) => {
            onTipoAjusteChange(t);
            if (t === "CORTESIA") onModoCalculoChange("FIJO");
          }}
        />

        <div>
          <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70 mb-2">Rápidos</p>
          <div className="flex flex-wrap gap-1.5">
            {PRESETS.map((p) => (
              <button
                key={p.label}
                type="button"
                onClick={() => {
                  onTipoAjusteChange(p.tipoAjuste);
                  onModoCalculoChange(p.modoCalculo);
                  onConceptoChange(p.concepto);
                  onValorChange(p.valor != null ? String(p.valor) : "");
                }}
                className="px-2.5 py-1 rounded-lg text-[10px] font-medium transition-all duration-150
                  bg-zinc-100/70 dark:bg-zinc-800/50 text-zinc-600 dark:text-zinc-400
                  hover:bg-zinc-200/70 dark:hover:bg-zinc-700/50 hover:text-zinc-900 dark:hover:text-zinc-200
                  active:scale-95 border border-zinc-200/50 dark:border-zinc-700/50"
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>

        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <span className="w-full border-t border-zinc-200/50 dark:border-zinc-700/50" />
          </div>
          <div className="relative flex justify-center">
            <span className="text-[10px] uppercase tracking-wider text-muted-foreground/50 bg-zinc-50 dark:bg-[#0c0c0e] px-2.5">Personalizado</span>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {tipoAjuste !== "CORTESIA" && (
            <AjusteConfigForm
              modoCalculo={modoCalculo}
              onModoCalculoChange={onModoCalculoChange}
              valor={valor}
              onValorChange={onValorChange}
            />
          )}
          <div className={tipoAjuste === "CORTESIA" ? "col-span-2" : ""}>
            <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70 mb-2">Detalles</p>
            <div className="space-y-1">
              <label className="text-[10px] font-medium text-muted-foreground/80">
                Concepto <span className="text-red-400">*</span>
              </label>
              <input
                type="text"
                value={concepto}
                onChange={(e) => onConceptoChange(e.target.value)}
                placeholder="Ej: IVA 19%, Descuento..."
                className="w-full h-9 px-3 rounded-lg text-sm outline-none transition-all duration-150
                  bg-white/60 dark:bg-zinc-900/60
                  border border-zinc-200/60 dark:border-zinc-700/60
                  hover:border-zinc-300 dark:hover:border-zinc-600
                  focus:border-emerald-500/50 focus:ring-[3px] focus:ring-emerald-500/15"
                autoComplete="off"
              />
            </div>
          </div>
        </div>

        {tipoAjuste === "CORTESIA" && (
          <div className="rounded-lg bg-purple-500/5 dark:bg-purple-500/10 border border-purple-200/30 dark:border-purple-500/20 px-3 py-2 text-xs text-purple-700 dark:text-purple-300/80">
            <p className="font-medium">Cortesía</p>
            <p className="text-[10px] text-purple-600/70 dark:text-purple-300/60 mt-0.5">Sin valor monetario. Solo concepto.</p>
          </div>
        )}
      </div>

      <div className="flex items-center justify-end gap-2 px-4 py-3 border-t border-zinc-200/40 dark:border-zinc-800/40 shrink-0">
        <Button
          variant="ghost"
          size="sm"
          onClick={onCancel}
          disabled={isPending}
          className="text-muted-foreground hover:text-foreground text-xs h-8 px-3"
        >
          Cancelar
        </Button>
        <Button
          size="sm"
          onClick={onAdd}
          disabled={!isValid || isPending}
          className="gap-1.5 bg-emerald-700 hover:bg-emerald-600 active:scale-95 text-white text-xs h-8 px-4 shadow-sm transition-all duration-150"
        >
          {isPending && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
          Añadir
        </Button>
      </div>
    </div>
  );
}

export default AjusteSidePanel;
