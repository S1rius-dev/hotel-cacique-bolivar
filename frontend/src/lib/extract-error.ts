import type { ApiError } from "@/api/custom-instance";

type UnknownRecord = Record<string, unknown>;

function isRecord(value: unknown): value is UnknownRecord {
  return typeof value === "object" && value !== null;
}

function readString(source: UnknownRecord, key: string): string | undefined {
  const value = source[key];
  return typeof value === "string" && value.length > 0 ? value : undefined;
}

const GENERIC_ERROR_MESSAGE = "Error inesperado";

function readCode(source: UnknownRecord): string | undefined {
  return readString(source, "code") ?? readString(source, "error");
}

/**
 * Lee errores por campo en las dos formas que emite el backend: el mapa
 * `fieldErrors: { campo: mensaje }` y el array `errors: [{ field, message }]`.
 */
export function readFieldErrors(source: UnknownRecord): Record<string, string> | undefined {
  const direct = source.fieldErrors;
  if (isRecord(direct)) {
    const entries = Object.entries(direct).filter(([, v]) => typeof v === "string");
    if (entries.length > 0) {
      return Object.fromEntries(entries) as Record<string, string>;
    }
  }

  const errors = source.errors;
  if (Array.isArray(errors)) {
    const fieldErrors: Record<string, string> = {};
    for (const entry of errors) {
      if (!isRecord(entry)) continue;
      const field = readString(entry, "field");
      const message = readString(entry, "message");
      if (field && message) fieldErrors[field] = message;
    }
    if (Object.keys(fieldErrors).length > 0) return fieldErrors;
  }

  return undefined;
}

/**
 * El interceptor de `custom-instance` normaliza los fallos a `ApiError`, pero
 * por seguridad se toleran también errores Axios crudos y envelops `{ data }`.
 *
 * El orden de las ramas importa: un `AxiosError` siempre trae `message`
 * ("Request failed with status code 500"), así que el envelope `response.data`
 * debe inspeccionarse ANTES del mensaje de nivel superior o el cuerpo del
 * backend nunca se lee.
 */
export function normalizeApiError(error: unknown): ApiError {
  if (isRecord(error)) {
    // 1) Axios crudo: { isAxiosError, response: { status, data } }
    const response = error.response;
    if (isRecord(response)) {
      const status = typeof response.status === "number" ? response.status : undefined;
      const data = response.data;
      if (!isRecord(data)) {
        return { message: GENERIC_ERROR_MESSAGE, status };
      }
      return {
        message:
          readString(data, "message") ?? readString(data, "detail") ?? GENERIC_ERROR_MESSAGE,
        code: readCode(data),
        fieldErrors: readFieldErrors(data),
        status,
      };
    }

    // 2) Fallo de transporte: sin `response` no hay mensaje del backend que mostrar.
    if (error.isAxiosError === true) {
      return { message: GENERIC_ERROR_MESSAGE };
    }

    // 3) ApiError ya normalizado por el interceptor
    const directMessage = readString(error, "message");
    const directFieldErrors = readFieldErrors(error);
    if (directMessage || directFieldErrors || typeof error.status === "number") {
      return {
        message: directMessage ?? GENERIC_ERROR_MESSAGE,
        code: readCode(error),
        fieldErrors: directFieldErrors,
        status: typeof error.status === "number" ? error.status : undefined,
      };
    }
  }

  // 4) Errores no-Axios (scripts de terceros, errores de runtime): se conserva el
  //    mensaje original para no ocultar diagnósticos reales.
  if (error instanceof Error) {
    return { message: error.message };
  }

  return { message: GENERIC_ERROR_MESSAGE };
}

export function extractErrorMessage(
  error: unknown,
  fallback = GENERIC_ERROR_MESSAGE,
): string {
  const { message } = normalizeApiError(error);
  return message === GENERIC_ERROR_MESSAGE ? fallback : message;
}

export function extractFieldErrors(error: unknown): Record<string, string> | undefined {
  return normalizeApiError(error).fieldErrors;
}
