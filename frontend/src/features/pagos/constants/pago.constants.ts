import {
  Banknote,
  CreditCard,
  Building2,
  FileText,
  Ellipsis,
} from "lucide-react";
import efectivoLogo from "@/assets/payment/efectivo.png";
import tarjetaLogo from "@/assets/payment/tarjeta.png";
import nequiLogo from "@/assets/payment/nequi.svg";
import pseLogo from "@/assets/payment/pse.svg";

export const TIPO_PAGO_OPTIONS = [
  { value: "TOTAL", label: "Pago completo" },
  { value: "PARCIAL", label: "Abono parcial" },
  { value: "ANTICIPO", label: "Anticipo" },
  { value: "DEPOSITO_GARANTIA", label: "Depósito de garantía" },
] as const;

export type TipoPagoValue = (typeof TIPO_PAGO_OPTIONS)[number]["value"];

export const METODO_PAGO_OPTIONS = [
  { value: "EFECTIVO", label: "Efectivo", icon: Banknote, logo: efectivoLogo },
  {
    value: "TARJETA_CREDITO",
    label: "Tarjeta de crédito",
    icon: CreditCard,
    logo: tarjetaLogo,
  },
  {
    value: "TARJETA_DEBITO",
    label: "Tarjeta de débito",
    icon: CreditCard,
    logo: tarjetaLogo,
  },
  { value: "TRANSFERENCIA", label: "Transferencia", icon: Building2 },
  { value: "CHEQUE", label: "Cheque", icon: FileText },
  { value: "NEQUI", label: "Nequi", icon: Banknote, logo: nequiLogo },
  { value: "PSE", label: "PSE", icon: Banknote, logo: pseLogo },
  { value: "OTRO", label: "Otro", icon: Ellipsis },
] as const;

export type MetodoPagoValue = (typeof METODO_PAGO_OPTIONS)[number]["value"];

function normalizeMetodo(value?: string | null): string {
  return String(value ?? "")
    .trim()
    .toUpperCase();
}

export function getMetodoPagoLabel(value?: string | null): string {
  const normalized = normalizeMetodo(value);
  const found = METODO_PAGO_OPTIONS.find((o) => o.value === normalized);
  if (found) return found.label;
  if (!normalized) return "—";
  // Fallback: humanizar snake case: TARJETA_CREDITO -> Tarjeta de crédito
  if (normalized === "TARJETA_CREDITO") return "Tarjeta de crédito";
  if (normalized === "TARJETA_DEBITO") return "Tarjeta de débito";
  // Capitalize generico
  return (
    normalized.charAt(0) + normalized.slice(1).toLowerCase().replace(/_/g, " ")
  );
}

export function getMetodoPagoOption(value?: string | null) {
  return (
    METODO_PAGO_OPTIONS.find((o) => o.value === normalizeMetodo(value)) ?? null
  );
}

export const ESTADO_PAGO_STYLES: Record<
  string,
  { label: string; className: string }
> = {
  COMPLETADO: {
    label: "Completado",
    className:
      "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800",
  },
  PENDIENTE: {
    label: "Pendiente",
    className:
      "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 border-amber-200 dark:border-amber-800",
  },
  REEMBOLSADO: {
    label: "Reembolsado",
    className:
      "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400 border-red-200 dark:border-red-800",
  },
  CANCELADO: {
    label: "Cancelado",
    className:
      "bg-zinc-100 text-zinc-700 dark:bg-zinc-900/30 dark:text-zinc-400 border-zinc-200 dark:border-zinc-800",
  },
} as const;

/**
 * Cómo se presenta cada caja de origen en el desglose de un pago.
 * El backend solo reparte entre RECEPCION y TIENDA.
 */
export const ORIGEN_COBRO_STYLES: Record<
  string,
  { label: string; className: string }
> = {
  RECEPCION: {
    label: "Recepción",
    className:
      "bg-blue-50 text-blue-700 dark:bg-blue-950/30 dark:text-blue-300 border-blue-200 dark:border-blue-900",
  },
  TIENDA: {
    label: "Tienda",
    className:
      "bg-amber-50 text-amber-700 dark:bg-amber-950/30 dark:text-amber-300 border-amber-200 dark:border-amber-900",
  },
} as const;

export function getOrigenCobroStyle(origen?: string | null) {
  return (
    ORIGEN_COBRO_STYLES[String(origen ?? "").toUpperCase()] ?? {
      label: "—",
      className:
        "bg-zinc-50 text-zinc-700 dark:bg-zinc-900/30 dark:text-zinc-300 border-zinc-200 dark:border-zinc-800",
    }
  );
}

export const FORMAT_CURRENCY = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});
