export function authRedirect(roles: string[]) {
  if (roles.includes("ROLE_CLIENTE")) return "/";

  if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_RECEPCIONISTA")) {
    return "/hotel/login";
  }

  return "/";
}
