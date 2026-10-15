import { getMetodoPagoOption } from "../constants/pago.constants";

interface Props {
  metodo: string | undefined | null;
  className?: string;
}

function PagoMetodoIcon({ metodo, className = "w-5 h-5" }: Props) {
  const option = getMetodoPagoOption(metodo) as unknown as { icon: React.ComponentType<{ className?: string }>; logo?: string } | null;
  if (!option) {
    return null;
  }

  // Si tiene logo (png/svg) priorizar imagen — permite usar assets/payment
  if (option.logo) {
    const isNequi = String(metodo ?? "").trim().toUpperCase() === "NEQUI";
    // Nequi SVG tiene mucho padding interno, se ve pequeño: escalarlo extra + aumentar base
    const logoClass = isNequi ? `${className} object-contain shrink-0 scale-[1.35]` : `${className} object-contain shrink-0`;
    return (
      <img
        src={option.logo}
        alt={String(metodo)}
        className={logoClass}
      />
    );
  }

  const Icon = option.icon;
  return <Icon className={className} />;
}

export default PagoMetodoIcon;
