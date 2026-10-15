import { useState } from "react";
import { customInstance } from "@/api/custom-instance";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { extractErrorMessage, normalizeApiError } from "@/lib/extract-error";

interface SearchState {
  status: "idle" | "searching" | "found" | "notFound" | "error";
  client: ClienteResponse | null;
  error?: string;
}

export function useClientSearch() {
  const [state, setState] = useState<SearchState>({
    status: "idle",
    client: null,
  });

  const search = async (
    tipoIdentificacionId: number,
    cedula: string,
  ) => {
    setState({ status: "searching", client: null });

    try {
      const client = await customInstance<ClienteResponse>({
        url: "/api/clientes/search",
        method: "GET",
        params: { tipoIdentificacionId, cedula },
      });

      setState({ status: "found", client });
    } catch (err: unknown) {
      const apiError = normalizeApiError(err);
      if (apiError.status === 404) {
        setState({ status: "notFound", client: null });
      } else {
        setState({
          status: "error",
          client: null,
          error: extractErrorMessage(err, "Error al buscar cliente"),
        });
      }
    }
  };

  const reset = () => {
    setState({ status: "idle", client: null });
  };

  return { ...state, search, reset };
}
