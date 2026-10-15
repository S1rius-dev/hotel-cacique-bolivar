export function formatCurrency(value?: number): string {
  if (value == null) return "";
  return `$${value.toLocaleString("es-CO")}`;
}

export function formatNumber(value?: number): string {
  if (value == null) return "";
  return value.toLocaleString("es-CO");
}
