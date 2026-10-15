import { Users, Info } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Props {
  capacidadBase: number;
  capacidadMaxima: number;
  ocupantes: number;
  onChange: (n: number) => void;
  touched: boolean;
  onTouch: () => void;
}

function QuickReservaOccupantsSelector({ capacidadBase, capacidadMaxima, ocupantes, onChange, touched, onTouch }: Props) {
  const isValid = ocupantes >= capacidadBase && ocupantes <= capacidadMaxima;

  const error =
    touched && !isValid
      ? ocupantes < capacidadBase
        ? `Mínimo ${capacidadBase} ocupante${capacidadBase > 1 ? "s" : ""}`
        : `Máximo ${capacidadMaxima} ocupante${capacidadMaxima > 1 ? "s" : ""}`
      : null;

  return (
    <div className="space-y-1.5">
      <label className="text-xs font-medium text-muted-foreground">
        Cantidad de ocupantes
      </label>
      <div className="flex items-center gap-3">
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            onTouch();
            onChange(Math.max(capacidadBase, ocupantes - 1));
          }}
          disabled={ocupantes <= capacidadBase}
        >
          -
        </Button>
        <div className="flex items-center gap-1.5 min-w-20 justify-center">
          <Users className="w-4 h-4 text-muted-foreground" />
          <span className="text-sm font-medium">{ocupantes}</span>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            onTouch();
            onChange(Math.min(capacidadMaxima, ocupantes + 1));
          }}
          disabled={ocupantes >= capacidadMaxima}
        >
          +
        </Button>
      </div>
      {error && (
        <p className="text-[11px] text-rose-600 flex items-center gap-1 mt-1">
          <Info className="w-3 h-3" />
          {error}
        </p>
      )}
      {ocupantes > capacidadBase && isValid && (
        <p className="text-[11px] text-amber-600 flex items-center gap-1 mt-1">
          <Info className="w-3 h-3" />
          {ocupantes - capacidadBase} persona
          {ocupantes - capacidadBase > 1 ? "s" : ""} extra
          {ocupantes - capacidadBase > 1 ? "s" : ""}
        </p>
      )}
    </div>
  );
}

export default QuickReservaOccupantsSelector;
