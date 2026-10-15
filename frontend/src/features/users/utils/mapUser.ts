import {
  UserRequestRolesItem,
  UserUpdateRequestRolesItem,
  type UserRequest,
  type UserUpdateRequest,
} from "@/api/generated/openAPIDefinition.schemas";
import type {
  UserFormValues,
  UserUpdateFormValues,
} from "../schemas/user.schema";

/**
 * Los roles se listan dinamicamente desde `/api/roles`, asi que el conjunto real
 * puede incluir roles mas alla de los valores sembrados que declara el OpenAPI.
 * Por eso el form trabaja con `string[]` y el ajuste al tipo generado ocurre
 * aqui, en el borde de la API, con un cast explicito y no con `any`.
 */
export const toApiRoles = (roles: string[]) => roles as UserRequestRolesItem[];

const toApiUpdateRoles = (roles: string[]) => roles as UserUpdateRequestRolesItem[];

export function toUserRequest(data: UserFormValues): UserRequest {
  return {
    email: data.email,
    username: data.username,
    password: data.password,
    roles: toApiRoles(data.roles),
  };
}

export function toUserUpdateRequest(data: UserUpdateFormValues): UserUpdateRequest {
  const payload: UserUpdateRequest = {
    // El DTO exige email y roles; password es opcional y solo viaja cuando el
    // usuario decide cambiarla.
    email: data.email,
    username: data.username,
    roles: toApiUpdateRoles(data.roles),
    activo: data.activo,
  };

  if (data.password) {
    payload.password = data.password;
  }

  return payload;
}
