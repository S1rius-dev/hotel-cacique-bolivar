import boldLogo from "@/assets/gateways/bold/Bold_Logo.svg";
import wompiLogo from "@/assets/gateways/wompi/Wompi_LogoSecundario.svg";
import { getApiUrl } from "@/api/api-config";

export const CANAL_EFECTIVO = "efectivo";
export const CANAL_SIN_FILTRO = "todos";

const GATEWAY_FALLBACK_KEY = "WOMPI";

const GATEWAY_LOGOS: Record<string, string> = {
  BOLD: boldLogo,
  WOMPI: wompiLogo,
};

const CANAL_LABELS: Record<string, string> = {
  wompi: "Wompi",
  bold: "Bold",
  mercado_pago: "Mercado Pago",
  payu_rapyd: "PayU",
  rapyd: "PayU",
};

const humanizar = (valor: string) =>
  valor
    .replace(/[_-]+/g, " ")
    .trim()
    .replace(/\b\w/g, (c) => c.toUpperCase());

export function gatewayKey(gateway: string | null | undefined): string {
  return (gateway ?? GATEWAY_FALLBACK_KEY).toUpperCase();
}

export function canalDeGateway(gateway: string | null | undefined): string {
  return gatewayKey(gateway).toLowerCase();
}

export function esCanalEfectivo(canal: string | null | undefined): boolean {
  return String(canal ?? "").toLowerCase() === CANAL_EFECTIVO;
}

/**
 * Un pago es online si el backend lo clasificó en una pasarela. Si `canal` llega
 * vacío se trata como no online: la rama online presupone campos de gateway
 * (método de pago, id de transacción) que un pago de caja no tiene.
 */
export function esCanalOnline(canal: string | null | undefined): boolean {
  const key = String(canal ?? "").toLowerCase();
  return key.length > 0 && key !== CANAL_EFECTIVO;
}

export function nombreCanal(
  canal: string | null | undefined,
  gateway: string | null | undefined,
  gatewayDisplayName: string,
): string {
  const key = String(canal ?? "").toLowerCase();
  if (esCanalEfectivo(key)) return "Efectivo";
  if (!key) return gatewayDisplayName;
  if (key === canalDeGateway(gateway)) return gatewayDisplayName;
  return CANAL_LABELS[key] ?? humanizar(key);
}

export function gatewayLogo(gateway: string | null | undefined): string {
  return GATEWAY_LOGOS[gatewayKey(gateway)] ?? wompiLogo;
}

export function resolveGatewayAssetUrl(
  logoUrl: string | null | undefined,
): string | null {
  const url = logoUrl?.trim();
  if (!url) return null;
  if (/^https?:\/\//i.test(url) || url.startsWith("data:")) return url;
  const path = url.startsWith("/") ? url : `/${url}`;
  return `${getApiUrl()}${path}`;
}
