import NotificationsIcon from "@/features/dashboard/icons/NotificationsIcon";
import SearchIcon from "@/features/dashboard/icons/SearchIcon";
import { formatDateTimeCompact } from "@/utils/date";
import { HOTEL } from "@/config/hotel";
import { useAuth } from "@/app/auth/AuthContext";
import { Globe, LogOut, Menu, Moon, Sun, User } from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useRef, useState, useSyncExternalStore } from "react";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { useNavigate, useLocation } from "react-router-dom";
import NotificationsDropdown from "@/features/notificaciones/components/NotificationsDropdown";
import NotificationDetailDialog from "@/features/notificaciones/components/NotificationDetailDialog";
import { useNotificaciones } from "@/features/notificaciones/hooks/useNotificaciones";
import { useNotificacionesNoLeidas } from "@/features/notificaciones/hooks/useNotificacionesNoLeidas";
import {
  useMarcarNotificacionLeida,
  useMarcarTodasLeidas,
} from "@/features/notificaciones/hooks/useNotificacionesMutations";
import type { Notificacion } from "@/features/notificaciones/types";

interface TopbarProps {
  onMenuClick?: () => void;
}

const ThemeToggle = () => {
  const { theme, setTheme } = useTheme();

  // `theme` llega del localStorage: en el servidor no existe, asi que se
  // difiere el render hasta hidratar. useSyncExternalStore lo resuelve sin
  // setState en un efecto.
  const hydrated = useSyncExternalStore(
    () => () => {},
    () => true,
    () => false,
  );
  if (!hydrated) return null;

  const isDark = theme === "dark";

  return (
    <button
      onClick={() => setTheme(isDark ? "light" : "dark")}
      className="
        p-2 rounded-md
        border border-border
        bg-card
        text-foreground
        transition-all duration-200
        hover:bg-muted
        hover:scale-105
        active:scale-95
      ">
      {isDark ? <Sun size={16} /> : <Moon size={16} />}
    </button>
  );
};

const Topbar: React.FC<TopbarProps> = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [currentDate, setCurrentDate] = useState(new Date());

  const [selectedNotification, setSelectedNotification] =
    useState<Notificacion | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [notifDropdownOpen, setNotifDropdownOpen] = useState(false);
  const [bellAnimating, setBellAnimating] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const prevUnreadCount = useRef<number | null>(null);

  const { data: notificacionesData, isLoading: notificacionesLoading } =
    useNotificaciones(0, 20);
  const { data: noLeidasData } = useNotificacionesNoLeidas();
  const marcarLeida = useMarcarNotificacionLeida();
  const marcarTodas = useMarcarTodasLeidas();

  const unreadCount = noLeidasData?.cantidad ?? 0;
  const notifications = notificacionesData ?? [];

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentDate(new Date());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (
      prevUnreadCount.current !== null &&
      unreadCount > prevUnreadCount.current &&
      unreadCount > 0
    ) {
      setBellAnimating(true);
      const t = setTimeout(() => setBellAnimating(false), 500);
      return () => clearTimeout(t);
    }
    prevUnreadCount.current = unreadCount;
  }, [unreadCount]);

  const handleSelectNotification = (n: Notificacion) => {
    setSelectedNotification(n);
    setDetailOpen(true);
    setNotifDropdownOpen(false);
  };

  const handleMarkAsRead = (id: number) => {
    marcarLeida.mutate(id);
  };

  const handleMarkAllRead = () => {
    marcarTodas.mutate();
  };

  const handleViewAll = () => {
    setNotifDropdownOpen(false);
  };

  const handleLogout = async () => {
    await logout();
    navigate("/hotel/login", { replace: true });
  };

  const handleSearch = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      const searchMap: Record<string, string> = {
        cliente: "/dashboard/clientes",
        clientes: "/dashboard/clientes",
        reserva: "/dashboard/reservas",
        reservas: "/dashboard/reservas",
        habitacion: "/dashboard/habitaciones",
        habitaciones: "/dashboard/habitaciones",
        cuarto: "/dashboard/habitaciones",
        cuartos: "/dashboard/habitaciones",
        pago: "/dashboard/reservas",
        pagos: "/dashboard/reservas",
        empleado: "/dashboard/empleados",
        empleados: "/dashboard/empleados",
        usuario: "/dashboard/usuarios",
        usuarios: "/dashboard/usuarios",
        ocupacion: "/dashboard/ocupacion",
        disponibilidad: "/dashboard/ocupacion",
        caja: "/dashboard/caja",
        turno: "/dashboard/caja",
        auditoria: "/dashboard/historial-acciones",
        auditoría: "/dashboard/historial-acciones",
        acciones: "/dashboard/historial-acciones",
        configuracion: "/dashboard/ajustes",
        configuración: "/dashboard/ajustes",
        ajustes: "/dashboard/ajustes",
      };
      const dest = searchMap[q] || (q.includes("client") ? "/dashboard/clientes" : null)
        || (q.includes("reserv") ? "/dashboard/reservas" : null)
        || (q.includes("habitac") ? "/dashboard/habitaciones" : null);
      if (dest) {
        navigate(dest);
        setSearchQuery("");
      }
    }
  };

  const handleProfileClick = () => {
    const background = {
      pathname: location.pathname,
      search: location.search,
    };
    navigate("/mi-perfil", {
      state: { background, profileVariant: "dashboard" },
    });
  };

  return (
    <header
      className="
        h-15
        flex items-center justify-between
        px-4 md:px-8
        bg-card
        border-b border-border
        shadow-[1px_0_0_rgba(0,0,0,0.04)]
      ">
      {/* LEFT */}
      <div className="flex items-center gap-2 text-sm">
        <button
          onClick={() => onMenuClick?.()}
          className="md:hidden p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition">
          <Menu size={20} />
        </button>

        <span className="font-heading font-bold text-foreground text-lg">
          Hotel Cacique de Bolivar
        </span>

        <span className="hidden sm:inline text-muted-foreground">›</span>

        <span className="hidden sm:inline text-muted-foreground text-sm">
          {formatDateTimeCompact(currentDate)}
        </span>
      </div>

      {/* RIGHT */}
      <div className="flex items-center gap-5">
        {/* NOTIFICATION */}
        <DropdownMenu
          open={notifDropdownOpen}
          onOpenChange={setNotifDropdownOpen}>
          <DropdownMenuTrigger asChild>
            <button className="relative text-muted-foreground hover:text-foreground transition cursor-pointer">
              <NotificationsIcon
                className={`w-5 h-5 ${bellAnimating ? "animate-bell-ring" : ""}`}
              />
              {unreadCount > 0 && (
                <span className="absolute -top-1.5 -right-1.5 flex items-center justify-center h-4 min-w-4 px-1 rounded-full bg-rose-600 text-[10px] font-bold text-white animate-badge-in">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </span>
              )}
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            align="end"
            className="p-0 rounded-xl border shadow-xl overflow-hidden max-sm:w-[calc(100vw-32px)] sm:w-130 max-sm:mr-4">
            <NotificationsDropdown
              notifications={notifications}
              loading={notificacionesLoading}
              onSelect={handleSelectNotification}
              onMarkAllRead={handleMarkAllRead}
              onViewAll={handleViewAll}
            />
          </DropdownMenuContent>
        </DropdownMenu>

        {/* SEARCH */}
        <div
          className="
            hidden sm:flex
            items-center
            gap-3
            px-4
            h-8
            w-80
            rounded-lg
            bg-card
            border border-border
          ">
          <SearchIcon className="w-4 h-4 text-muted-foreground" />

          <input
            type="text"
            placeholder="Buscar (Enter para ir)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={handleSearch}
            className="
              flex-1
              text-sm
              bg-transparent
              focus:outline-none
              placeholder:text-muted-foreground
              text-foreground
            "
          />
        </div>

        {/* DARK MODE */}
        <ThemeToggle />

        {/* USER PROFILE */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              className={`
                cursor-pointer rounded-full
                transition-all duration-200
                hover:scale-105 active:scale-95
                ${user?.profilePicture ? "p-0" : "p-2 border border-border bg-card text-foreground hover:bg-muted"}
              `}>
              {user?.profilePicture ? (
                <img
                  src={user.profilePicture}
                  alt=""
                  className="size-9 rounded-full object-cover ring-2 ring-border"
                />
              ) : (
                <User size={20} />
              )}
            </button>
          </DropdownMenuTrigger>

          <DropdownMenuContent
            align="end"
            className="w-60 rounded-xl border shadow-xl p-2">
            <div className="px-2 py-3 border-b">
              <p className="font-medium">{user?.username ?? "Usuario"}</p>
              <p className="text-xs text-muted-foreground">
                {user?.email ?? ""}
              </p>
            </div>

            <DropdownMenuItem onClick={handleProfileClick} className="cursor-pointer gap-3 rounded-lg py-2">
              <User size={16} />
              <span>Mi perfil</span>
            </DropdownMenuItem>

            <DropdownMenuSeparator />

            <DropdownMenuItem
              onClick={() =>
                window.open(HOTEL.baseUrl, "_blank")
              }
              className="cursor-pointer gap-3 rounded-lg py-2">
              <Globe size={16} />
              <span>Ver sitio web</span>
            </DropdownMenuItem>

            <DropdownMenuSeparator />

            <DropdownMenuItem
              onClick={handleLogout}
              className="cursor-pointer gap-3 rounded-lg py-2 text-red-500 focus:text-red-500">
              <LogOut size={16} />
              <span>Cerrar sesión</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <NotificationDetailDialog
        notification={selectedNotification}
        open={detailOpen}
        onOpenChange={setDetailOpen}
        onMarkAsRead={handleMarkAsRead}
      />
    </header>
  );
};

export default Topbar;
