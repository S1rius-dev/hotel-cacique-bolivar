import { Plus, Minus, Gift, Trash2 } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { AjusteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  ajuste: AjusteResponse;
  readonly?: boolean;
  onDelete?: (ajuste: AjusteResponse) => void;
}

const TIPO_STYLES: Record<string, { label: string; className: string; icon: typeof Plus; sign: 1 | -1 | 0 }> = {
  CARGO: {
    label: "Cargo",
    className: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/30 dark:text-amber-400 dark:border-amber-800",
    icon: Plus,
    sign: 1,
  },
  DESCUENTO: {
    label: "Descuento",
    className: "bg-green-50 text-green-700 border-green-200 dark:bg-green-950/30 dark:text-green-400 dark:border-green-800",
    icon: Minus,
    sign: -1,
  },
  CORTESIA: {
    label: "Cortesía",
    className: "bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/30 dark:text-purple-400 dark:border-purple-800",
    icon: Gift,
    sign: 0,
  },
};

function AjusteCard({ ajuste, readonly, onDelete }: Props) {
  const tipo = ajuste.tipoAjuste ?? "CARGO";
  const style = TIPO_STYLES[tipo] ?? TIPO_STYLES.CARGO;
  const Icon = style.icon;

  const valorDisplay = () => {
    if (ajuste.valor == null) return null;
    if (ajuste.modoCalculo === "TOTAL") {
      return `→ ${FORMAT_CURRENCY.format(ajuste.valor)}`;
    }
    if (ajuste.modoCalculo === "PORCENTUAL") {
      return `${ajuste.valor}%`;
    }
    const prefix = style.sign > 0 ? "+" : style.sign < 0 ? "-" : "";
    return `${prefix}${FORMAT_CURRENCY.format(ajuste.valor)}`;
  };

  return (
    <div className="flex items-center gap-2 px-3 py-2 rounded-md border bg-zinc-50 dark:bg-zinc-900/40 text-[13px]">
      <span className={`inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full border font-medium shrink-0 ${style.className}`}>
        <Icon className="w-3 h-3" />
        {style.label}
      </span>
      {ajuste.activo === false && (
        <span className="text-[11px] text-muted-foreground bg-zinc-200/60 dark:bg-zinc-800 px-2 py-0.5 rounded shrink-0">
          Inactivo
        </span>
      )}
      <span className="min-w-0 flex-1 truncate text-zinc-700 dark:text-zinc-300">
        {ajuste.concepto}
      </span>
      {valorDisplay() && (
        <span className={`font-semibold shrink-0 ${
          style.sign > 0 ? "text-amber-600" : style.sign < 0 ? "text-green-600" : "text-muted-foreground"
        }`}>
          {valorDisplay()}
        </span>
      )}
      {!readonly && onDelete && (
        <button
          onClick={() => onDelete(ajuste)}
          className="shrink-0 p-1 rounded text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
}

export default AjusteCard;
