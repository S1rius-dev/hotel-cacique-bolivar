import { motion } from "motion/react";
import { Sparkles, X, CheckSquare, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Props {
  selectedCount: number;
  totalCount: number;
  onSelectAll: () => void;
  onDeselectAll: () => void;
  onCancel: () => void;
  onMarcarLimpias: () => void;
  isPending: boolean;
}

function AccionesMasivasBar({
  selectedCount,
  totalCount,
  onSelectAll,
  onDeselectAll,
  onCancel,
  onMarcarLimpias,
  isPending,
}: Props) {
  return (
    <motion.div
      initial={{ opacity: 0, y: -12, height: 0 }}
      animate={{ opacity: 1, y: 0, height: "auto" }}
      exit={{ opacity: 0, y: -12, height: 0 }}
      transition={{ type: "spring", stiffness: 300, damping: 28 }}
      className="overflow-hidden"
    >
      <div className="mb-4 p-4 rounded-2xl bg-white dark:bg-zinc-900 border border-sky-200 dark:border-sky-800 shadow-sm">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-sky-100 dark:bg-sky-900/30">
              <Sparkles className="w-4 h-4 text-sky-500" />
            </div>
            <span className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">
              Limpieza masiva
            </span>
          </div>
          <button
            onClick={onCancel}
            className="p-1 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors text-zinc-400 dark:text-zinc-500 hover:text-zinc-600 dark:hover:text-zinc-300"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="flex flex-wrap items-center gap-x-3 md:gap-x-6 gap-y-1.5 md:gap-y-2">
          <span className="text-xs md:text-sm text-zinc-600 dark:text-zinc-400">
            <span className="font-semibold text-zinc-900 dark:text-zinc-100">{selectedCount}</span>{" "}
            {selectedCount === 1 ? "habitación seleccionada" : "habitaciones seleccionadas"}
            {" — "}
            <span className="text-zinc-500 dark:text-zinc-400">{totalCount} en limpieza</span>
          </span>

          {totalCount > 0 && (
            <div className="hidden md:block flex-1 min-w-20 max-w-32 h-1.5 rounded-full bg-zinc-100 dark:bg-zinc-800 overflow-hidden">
              <div
                className="h-full rounded-full bg-sky-500 transition-all duration-300"
                style={{ width: `${(selectedCount / totalCount) * 100}%` }}
              />
            </div>
          )}

          <div className="flex items-center gap-1 md:gap-2 ml-auto">
            <button
              onClick={onSelectAll}
              disabled={selectedCount === totalCount}
              className="inline-flex items-center gap-1 md:gap-1.5 text-[10px] md:text-xs px-2 md:px-2.5 py-1 md:py-1.5 rounded-lg bg-zinc-100 dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 hover:text-zinc-800 dark:hover:text-zinc-100 transition-colors border border-zinc-200 dark:border-zinc-700 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <CheckSquare className="size-3 md:size-3.5" />
              <span className="hidden sm:inline">Seleccionar todas</span>
              <span className="sm:hidden">Todas</span>
            </button>
            <button
              onClick={onDeselectAll}
              disabled={selectedCount === 0}
              className="inline-flex items-center gap-1 md:gap-1.5 text-[10px] md:text-xs px-2 md:px-2.5 py-1 md:py-1.5 rounded-lg bg-zinc-100 dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 hover:text-zinc-800 dark:hover:text-zinc-100 transition-colors border border-zinc-200 dark:border-zinc-700 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <RotateCcw className="size-3 md:size-3.5" />
              <span className="hidden sm:inline">Deseleccionar todas</span>
              <span className="sm:hidden">Ninguna</span>
            </button>
            <Button
              size="sm"
              disabled={selectedCount === 0 || isPending}
              onClick={onMarcarLimpias}
              className="gap-1 md:gap-1.5 bg-sky-600 hover:bg-sky-500 text-white shadow-sm disabled:opacity-40 text-[10px] md:text-xs h-auto md:h-8 px-2 md:px-3 py-1 md:py-1.5"
            >
              {isPending ? (
                <span className="w-3 md:w-3.5 h-3 md:h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Sparkles className="size-3 md:size-3.5" />
              )}
              Limpias ({selectedCount})
            </Button>
          </div>
        </div>
      </div>
    </motion.div>
  );
}

export default AccionesMasivasBar;
