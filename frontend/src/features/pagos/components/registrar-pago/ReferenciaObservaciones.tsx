interface Props {
  referencia: string;
  observaciones: string;
  onReferenciaChange: (valor: string) => void;
  onObservacionesChange: (valor: string) => void;
}

const FIELD_CLASS =
  "w-full rounded-lg px-3 text-sm outline-none transition-all duration-150 \
  bg-white/60 dark:bg-zinc-900/60 \
  border border-zinc-200/60 dark:border-zinc-700/60 \
  hover:border-zinc-300 dark:hover:border-zinc-600 \
  focus:border-emerald-500/50 focus:ring-[3px] focus:ring-emerald-500/15";

function ReferenciaObservaciones({ referencia, observaciones, onReferenciaChange, onObservacionesChange }: Props) {
  return (
    <div className="flex-1 min-h-0 grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div className="space-y-2">
        <label className="text-xs font-medium text-muted-foreground/80">
          Referencia <span className="text-muted-foreground/40">(opcional)</span>
        </label>
        <input
          type="text"
          value={referencia}
          onChange={(e) => onReferenciaChange(e.target.value)}
          placeholder="N° de transacción"
          className={`${FIELD_CLASS} h-10`}
          autoComplete="off"
        />
      </div>

      <div className="flex min-h-0 flex-col gap-2">
        <label className="text-xs font-medium text-muted-foreground/80">
          Observaciones <span className="text-muted-foreground/40">(opcional)</span>
        </label>
        <textarea
          value={observaciones}
          onChange={(e) => onObservacionesChange(e.target.value)}
          placeholder="Notas adicionales"
          rows={2}
          className={`${FIELD_CLASS} flex-1 min-h-20 max-h-56 resize-none py-2.5 leading-relaxed`}
        />
      </div>
    </div>
  );
}

export default ReferenciaObservaciones;
