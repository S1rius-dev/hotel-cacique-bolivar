import { Wallet } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";

interface Props {
  total: number;
  totalPagado: number;
  saldoPendiente: number;
}

function RoomPaymentLabel({ total, totalPagado, saldoPendiente }: Props) {
  if (total <= 0) return null;

  const isPaid = saldoPendiente <= 0;
  const isPartial = saldoPendiente > 0 && totalPagado > 0;

  let text: string;
  let colorClass: string;

  if (isPaid) {
    text = `Pagado ${FORMAT_CURRENCY.format(totalPagado)}`;
    colorClass = "text-emerald-600 dark:text-emerald-400";
  } else if (isPartial) {
    text = `${FORMAT_CURRENCY.format(saldoPendiente)} pendientes`;
    colorClass = "text-amber-600 dark:text-amber-400";
  } else {
    text = `${FORMAT_CURRENCY.format(saldoPendiente)} sin pagar`;
    colorClass = "text-red-600 dark:text-red-400";
  }

  return (
    <div className={`flex items-center gap-1 text-[11px] font-medium ${colorClass} ml-3.5`}>
      <Wallet className="w-3 h-3 shrink-0" />
      <span className="truncate">{text}</span>
    </div>
  );
}

export default RoomPaymentLabel;
