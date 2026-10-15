import AjusteCard from "@/features/pagos/components/AjusteCard";
import type { AjusteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  ajustes: AjusteResponse[];
  onDelete: (ajuste: AjusteResponse) => void;
}

function AjustesAplicadosList({ ajustes, onDelete }: Props) {
  if (ajustes.length === 0) return null;

  return (
    <div className="space-y-2.5">
      <p className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground/70">
        Ajustes aplicados
      </p>
      <div className="space-y-2">
        {ajustes.map((ajuste) => (
          <AjusteCard
            key={ajuste.id}
            ajuste={ajuste}
            onDelete={onDelete}
          />
        ))}
      </div>
    </div>
  );
}

export default AjustesAplicadosList;
