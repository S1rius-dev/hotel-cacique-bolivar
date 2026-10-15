import { ESTADO_PAGO_STYLES } from "../constants/pago.constants";

interface Props {
  estado: string | undefined;
}

const ESTADO_DOT_CLASSES: Record<string, string> = {
  COMPLETADO: "bg-emerald-500",
  PENDIENTE: "bg-amber-500",
  REEMBOLSADO: "bg-red-500",
  CANCELADO: "bg-zinc-400",
};

function PagoStatusBadge({ estado }: Props) {
  const style = ESTADO_PAGO_STYLES[estado ?? ""];
  if (!style) return null;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[11px] font-medium border ${style.className}`}
    >
      <span
        className={`w-1.5 h-1.5 rounded-full ${ESTADO_DOT_CLASSES[estado ?? ""] ?? "bg-zinc-400"}`}
      />
      {style.label}
    </span>
  );
}

export default PagoStatusBadge;
