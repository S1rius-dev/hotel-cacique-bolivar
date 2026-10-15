import { Plus, Minus, Gift, CreditCard, Banknote, Building2, FileText, Ellipsis } from "lucide-react";
import efectivoLogo from "@/assets/payment/efectivo.png";
import tarjetaLogo from "@/assets/payment/tarjeta.png";
import nequiLogo from "@/assets/payment/nequi.svg";
import pseLogo from "@/assets/payment/pse.svg";

export const METODOS = [
  { value: "EFECTIVO", label: "Efectivo", icon: Banknote, logo: efectivoLogo },
  { value: "TARJETA_CREDITO", label: "Crédito", icon: CreditCard, logo: tarjetaLogo },
  { value: "TARJETA_DEBITO", label: "Débito", icon: CreditCard, logo: tarjetaLogo },
  { value: "TRANSFERENCIA", label: "Transferencia", icon: Building2 },
  { value: "CHEQUE", label: "Cheque", icon: FileText },
  { value: "NEQUI", label: "Nequi", icon: Banknote, logo: nequiLogo },
  { value: "PSE", label: "PSE", icon: Banknote, logo: pseLogo },
  { value: "OTRO", label: "Otro", icon: Ellipsis },
];

export const TIPOS_AJUSTE = [
  { value: "CARGO", label: "Cargo", subtitle: "Agregar al total", icon: Plus, color: "amber" },
  { value: "DESCUENTO", label: "Descuento", subtitle: "Restar del total", icon: Minus, color: "green" },
  { value: "CORTESIA", label: "Cortesía", subtitle: "Sin costo", icon: Gift, color: "purple" },
] as const;

export const PRESETS = [
  { label: "IVA 19%", tipoAjuste: "CARGO", modoCalculo: "PORCENTUAL", concepto: "IVA 19%", valor: 19 },
  { label: "Desc. 10%", tipoAjuste: "DESCUENTO", modoCalculo: "PORCENTUAL", concepto: "Descuento 10%", valor: 10 },
  { label: "Depósito garantía", tipoAjuste: "CARGO", modoCalculo: "FIJO", concepto: "Depósito de garantía", valor: undefined },
  { label: "Cargo mascota", tipoAjuste: "CARGO", modoCalculo: "FIJO", concepto: "Cargo por mascota", valor: undefined },
  { label: "Cortesía", tipoAjuste: "CORTESIA", modoCalculo: "FIJO", concepto: "Cortesía", valor: undefined },
] as const;

export const TIPO_GLOW: Record<string, string> = {
  amber: "shadow-[0_0_20px_-4px_rgba(245,158,11,0.3)] ring-2 ring-amber-500/50",
  green: "shadow-[0_0_20px_-4px_rgba(34,197,94,0.3)] ring-2 ring-green-500/50",
  purple: "shadow-[0_0_20px_-4px_rgba(168,85,247,0.3)] ring-2 ring-purple-500/50",
};

export const TIPO_BG: Record<string, string> = {
  amber: "bg-gradient-to-br from-amber-500/10 via-amber-500/5 to-transparent dark:from-amber-500/15 dark:via-amber-500/5",
  green: "bg-gradient-to-br from-green-500/10 via-green-500/5 to-transparent dark:from-green-500/15 dark:via-green-500/5",
  purple: "bg-gradient-to-br from-purple-500/10 via-purple-500/5 to-transparent dark:from-purple-500/15 dark:via-purple-500/5",
};

export const TIPO_ICON_BG: Record<string, string> = {
  amber: "bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300",
  green: "bg-green-100 text-green-700 dark:bg-green-500/20 dark:text-green-300",
  purple: "bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-300",
};
