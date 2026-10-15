import type { UserResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { SessionUser } from "./AuthContext";

/**
 * `UserResponse` llega con todos sus campos opcionales, asi que aqui se aplican
 * los defaults que la app espera en `SessionUser`.
 */
export function toSessionUser(data: UserResponse): SessionUser {
  return {
    id: data.id,
    email: data.email ?? "",
    username: data.username,
    roles: (data.roles ?? []).map((r) => (typeof r === "string" ? r : String(r))),
    clienteId: data.clienteId,
    profilePicture: data.profilePicture,
  };
}
