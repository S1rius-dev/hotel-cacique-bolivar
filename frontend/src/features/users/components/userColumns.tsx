import type { Column } from "@/components/ui/table/data-table/types";
import type { UserResponse } from "@/api/generated/openAPIDefinition.schemas";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { formatDateTimeCompact } from "@/utils/date";

export const userColumns: Column<UserResponse>[] = [
  {
    key: "email",
    header: "Email",
    className: "font-medium max-md:hidden",
    cellClassName: "truncate",
  },
  {
    key: "username",
    header: "Usuario",
    className: "text-center",
    cellClassName: "truncate",
  },
  {
    key: "roles",
    header: "Rol",
    className: "text-center",
    render: (user) => (
      <span className="inline-flex items-center gap-1 rounded-full bg-violet-50 text-violet-700 border border-violet-200 dark:bg-violet-950 dark:text-violet-400 dark:border-violet-800 px-2.5 py-0.5 text-xs font-medium">
        {user.roles?.join(", ") ?? "-"}
      </span>
    ),
  },
  {
    key: "activo",
    header: "Estado",
    className: "text-center max-md:hidden",
    render: (user) => <StatusBadge active={user.activo} />,
  },
  {
    key: "lastLogin",
    header: "Último login",
    className: "text-center max-lg:hidden",
    render: (user) => formatDateTimeCompact(user.lastLogin),
  },
  {
    key: "provider",
    header: "Proveedor",
    className: "text-center font-medium uppercase text-xs max-lg:hidden",
    render: (user) => user.provider ?? "-",
  },
];
