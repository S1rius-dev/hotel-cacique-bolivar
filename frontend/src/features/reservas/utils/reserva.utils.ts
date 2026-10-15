export function formatCurrency(value?: number) {
  if (value == null) return "";
  return `$${value.toLocaleString("es-CO")}`;
}
