import { parseMonto } from "./utils";
import { formatNumber } from "@/utils/format";

interface Props {
  monto: number;
  saldoPendiente: number;
  tipoPago: string;
  onMontoChange: (monto: number) => void;
  onTipoPagoChange: (tipo: string) => void;
}

const FIELD_CLASS =
  "w-full h-10 rounded-lg px-3 text-sm outline-none transition-all duration-150 \
  bg-white/60 dark:bg-zinc-900/60 \
  border border-zinc-200/60 dark:border-zinc-700/60 \
  hover:border-zinc-300 dark:hover:border-zinc-600 \
  focus:border-emerald-500/50 focus:ring-[3px] focus:ring-emerald-500/15";

function PaymentFields({ monto, saldoPendiente, tipoPago, onMontoChange, onTipoPagoChange }: Props) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div className="space-y-2">
        <label className="text-xs font-medium text-muted-foreground/80">Monto</label>
        <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground font-medium pointer-events-none">$</span>
          <input
            type="text"
            inputMode="numeric"
            value={monto > 0 ? formatNumber(monto) : ""}
            placeholder="0"
            onChange={(e) => {
              const val = parseMonto(e.target.value);
              onMontoChange(val);
            }}
            className={`${FIELD_CLASS} pl-7 tabular-nums`}
            autoComplete="off"
          />
        </div>
        {monto > saldoPendiente && (
          <p className="text-xs text-red-500 flex items-center gap-1">
            <span className="w-1 h-1 rounded-full bg-red-500 inline-block" />
            Supera el saldo pendiente
          </p>
        )}
      </div>

      <div className="space-y-2">
        <label className="text-xs font-medium text-muted-foreground/80">Tipo de pago</label>
        <select
          value={tipoPago}
          onChange={(e) => onTipoPagoChange(e.target.value)}
          className={FIELD_CLASS}
        >
          <option value="TOTAL">Pago completo</option>
          <option value="PARCIAL">Abono parcial</option>
          <option value="ANTICIPO">Anticipo</option>
          <option value="DEPOSITO_GARANTIA">Depósito de garantía</option>
        </select>
      </div>
    </div>
  );
}

export default PaymentFields;
