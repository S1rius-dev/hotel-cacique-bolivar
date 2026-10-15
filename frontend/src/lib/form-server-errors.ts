import type { FieldValues, Path, UseFormSetError } from "react-hook-form";
import { extractFieldErrors } from "@/lib/extract-error";

/**
 * Mapea los `fieldErrors` que devuelve el backend a los campos del formulario.
 * Devuelve `true` si hubo errores de campo, para que el llamador decida si
 * muestra un mensaje genérico o el detalle del fallo.
 */
export function applyServerFieldErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
): boolean {
  const fieldErrors = extractFieldErrors(error);
  if (!fieldErrors) return false;

  for (const [field, message] of Object.entries(fieldErrors)) {
    setError(field as Path<T>, { type: "server", message: String(message) });
  }
  return true;
}
