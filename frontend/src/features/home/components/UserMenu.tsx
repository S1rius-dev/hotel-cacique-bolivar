import { useState, type ReactNode } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import ProfileIcon from "@/features/auth-client/icons/ProfileIcon";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { CalendarDays, LayoutDashboard, LogOut } from "lucide-react";
import type { SessionUser } from "@/app/auth/AuthContext";
import { useProfileImage } from "@/lib/useProfileImage";

interface Props {
  user: SessionUser;
  scrolled: boolean;
  isStaff: boolean;
  needsClientProfile: boolean;
  onCompleteProfile: () => void;
  onLogout: () => void;
  /** true dentro del menú móvil: muestra las opciones en línea, sin desplegable. */
  inline?: boolean;
  /** Se llama después de cualquier acción (útil para cerrar el menú móvil). */
  onAction?: () => void;
  /** Estilo blanco opcional. Por defecto usa los colores del tema. */
  light?: boolean;
}

/**
 * Estilos por modo. `force-light` desactiva las variantes `dark:` (ver @custom-variant dark)
 * y va también en el contenido del desplegable porque se renderiza en un portal,
 * fuera del árbol del componente.
 */
const LIGHT = {
  force: "force-light",
  panel: "border-[#EFEAE3] bg-white text-[#241F1D]",
  name: "text-[#241F1D]",
  muted: "text-[#8A7F76]",
  divider: "border-[#EFEAE3]",
  separator: "bg-[#EFEAE3]",
  item: "text-[#241F1D] focus:bg-[#FAF7F2] focus:text-[#241F1D]",
  logout: "text-red-600 focus:bg-red-50 focus:text-red-600",
  card: "rounded-2xl border border-[#EFEAE3] bg-[#FAF7F2] p-3 text-[#241F1D]",
  row: "text-[#241F1D] hover:bg-white active:bg-[#F5F0EA]",
  rowLogout: "text-red-600 hover:bg-red-50 active:bg-red-100",
};

const THEMED = {
  force: "",
  panel: "bg-popover text-popover-foreground",
  name: "",
  muted: "text-muted-foreground",
  divider: "",
  separator: "",
  item: "",
  logout: "text-red-500 focus:text-red-500",
  card: "text-white",
  row: "text-white/90 hover:bg-white/10 active:bg-white/15",
  rowLogout: "text-red-400 hover:bg-white/10 active:bg-white/15",
};

/** Lista en línea sobre panel blanco (cuando `scrolled` es true). */
const ON_LIGHT_BG = {
  card: "text-[#241F1D]",
  muted: "text-[#8A7F76]",
  divider: "border-[#EFEAE3]",
  row: "text-[#241F1D] hover:bg-black/5 active:bg-black/10",
  rowLogout: "text-red-600 hover:bg-red-50 active:bg-red-100",
};

/** Lista en línea sobre panel oscuro (arriba de la página, sin scroll). */
const ON_DARK_BG = {
  card: "text-white",
  muted: "text-white/60",
  divider: "border-white/10",
  row: "text-white/90 hover:bg-white/10 active:bg-white/15",
  rowLogout: "text-red-400 hover:bg-white/10 active:bg-white/15",
};

function getInitials(name: string) {
  const parts = name
    .trim()
    .split(/[\s._-]+/)
    .filter(Boolean);
  return (
    parts.length > 1 ? parts[0][0] + parts[1][0] : name.slice(0, 2)
  ).toUpperCase();
}

/** Foto con respaldo: si la URL falla, muestra las iniciales en vez del icono roto. */
function UserAvatar({
  src,
  name,
  className,
}: {
  src?: string | null;
  name: string;
  className: string;
}) {
  const [failed, setFailed] = useState(false);
  if (src && !failed) {
    return (
      <img
        src={src}
        alt=""
        referrerPolicy="no-referrer"
        onError={() => setFailed(true)}
        className={`${className} rounded-full object-cover ring-2 ring-white/80`}
      />
    );
  }
  return (
    <span
      aria-hidden="true"
      className={`${className} flex items-center justify-center rounded-full bg-rose-100 text-xs font-medium text-rose-800 ring-2 ring-white/80`}>
      {getInitials(name)}
    </span>
  );
}

export default function UserMenu({
  user,
  scrolled,
  isStaff,
  needsClientProfile,
  onCompleteProfile,
  onLogout,
  inline = false,
  onAction,
  light: lightProp,
}: Props) {
  const navigate = useNavigate();
  const location = useLocation();
  const imgSrc = useProfileImage(user.profilePicture);
  const name = user.username ?? user.email ?? "Mi cuenta";

  const light = lightProp ?? false;
  const s = light ? LIGHT : THEMED;
  const t = light
    ? {
        card: s.card,
        muted: s.muted,
        divider: s.divider,
        row: s.row,
        rowLogout: s.rowLogout,
      }
    : scrolled
      ? ON_LIGHT_BG
      : ON_DARK_BG;

  const run = (fn: () => void) => () => {
    fn();
    onAction?.();
  };

  const actions: {
    key: string;
    label: string;
    icon: ReactNode;
    onSelect: () => void;
  }[] = [
    {
      key: "perfil",
      label: "Mi perfil",
      icon: <ProfileIcon className="size-4" />,
      onSelect: run(() =>
        navigate("/mi-perfil", { state: { background: location } }),
      ),
    },
    isStaff
      ? {
          key: "dashboard",
          label: "Dashboard",
          icon: <LayoutDashboard className="size-4" />,
          onSelect: run(() => navigate("/dashboard")),
        }
      : {
          key: "reservas",
          label: "Mis reservas",
          icon: <CalendarDays className="size-4" />,
          onSelect: run(() => {
            if (needsClientProfile) return onCompleteProfile();
            navigate("/mis-reservas", { state: { background: location } });
          }),
        },
  ];

  const handleLogout = run(onLogout);

  const inlineView = (
    <div className={`${s.force} ${t.card}`}>
      <div className="flex items-center gap-3 px-1">
        <UserAvatar src={imgSrc} name={name} className="size-10 shrink-0" />
        <div className="min-w-0">
          <p className="truncate text-[15px] font-medium">{name}</p>
          <p className={`truncate text-xs ${t.muted}`}>{user.email}</p>
        </div>
      </div>

      <ul className={`mt-3 grid gap-0.5 border-t pt-2 ${t.divider}`}>
        {actions.map((a) => (
          <li key={a.key}>
            <button
              type="button"
              onClick={a.onSelect}
              className={`flex min-h-11 w-full cursor-pointer items-center gap-3 rounded-lg px-3 text-left text-[15px] transition ${t.row}`}>
              {a.icon}
              {a.label}
            </button>
          </li>
        ))}
        <li>
          <button
            type="button"
            onClick={handleLogout}
            className={`flex min-h-11 w-full cursor-pointer items-center gap-3 rounded-lg px-3 text-left text-[15px] transition ${t.rowLogout}`}>
            <LogOut className="size-4" />
            Cerrar sesión
          </button>
        </li>
      </ul>
    </div>
  );

  const dropdownView = (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button
          aria-label="Abrir menú de usuario"
          className={`
            cursor-pointer rounded-full
            transition-all duration-200 hover:scale-105
            ${imgSrc ? "p-0" : "p-3 backdrop-blur-sm border " + (scrolled ? "bg-rose-50 border-rose-200 text-rose-600 hover:bg-rose-100" : "bg-white/10 border-white/20 text-white hover:bg-white/20")}
          `}>
          {imgSrc ? (
            <UserAvatar src={imgSrc} name={name} className="size-8" />
          ) : (
            <ProfileIcon className="text-inherit text-2xl" />
          )}
        </button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        collisionPadding={12}
        className={`w-60 rounded-xl border p-2 shadow-xl ${s.force} ${s.panel}`}>
        <div className={`border-b px-2 py-3 ${s.divider}`}>
          <p className={`font-medium ${s.name}`}>{user.username}</p>
          <p className={`text-xs ${s.muted}`}>{user.email}</p>
        </div>

        {actions.map((a) => (
          <DropdownMenuItem
            key={a.key}
            onClick={a.onSelect}
            className={`cursor-pointer gap-3 rounded-lg py-2 ${s.item}`}>
            {a.icon}
            <span>{a.label}</span>
          </DropdownMenuItem>
        ))}

        <DropdownMenuSeparator className={s.separator} />

        <DropdownMenuItem
          onClick={handleLogout}
          className={`cursor-pointer gap-3 rounded-lg py-2 ${s.logout}`}>
          <LogOut className="size-4" />
          <span>Cerrar sesión</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );

  if (inline) return inlineView;

  // Lista en línea en móvil, desplegable desde `md`. Ajusta `md` al breakpoint de tu navbar.
  return (
    <>
      <div className="md:hidden">{inlineView}</div>
      <div className="hidden md:contents">{dropdownView}</div>
    </>
  );
}
