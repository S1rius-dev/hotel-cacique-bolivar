import { useEffect, useMemo, useState } from "react";
import Sidebar from "@/layouts/dashboard/Sidebar";
import type { SidebarSection } from "@/layouts/dashboard/Sidebar";
import Topbar from "@/layouts/dashboard/Topbar";
import Logo from "@/assets/logo.png";
import {
  CalendarCheck,
  ChartColumn,
  History,
  Wallet,
  FileText,
  ClipboardList,
  CreditCard,
  ShoppingBag,
  DatabaseBackup,
} from "lucide-react";
import ClientsIcon from "@/features/dashboard/icons/ClientsIcon";
import ReservaIcon from "@/features/dashboard/icons/ReservaIcon";
import RoomIcon from "@/features/dashboard/icons/RoomIcon";
import EmpleadoIcon from "@/features/dashboard/icons/EmpleadoIcon";
import UserIcon from "@/features/dashboard/icons/UserIcon";
import SettingsIcon from "@/features/dashboard/icons/SettingsIcon";
import { useAuth } from "@/app/auth/AuthContext";
import { Outlet } from "react-router-dom";
import { SEOHead } from "@/components/seo";

/**
 * Los items con `roles` restringidos son ADMIN-only tambien en el backend
 * (/api/users/**, /api/empleados/**, /api/audit-logs/** y /api/backups/**). El campo existia
 * en `SidebarItems` pero nunca se leyo, asi que antes eran visibles para RECEPCIONISTA y cada
 * clic terminaba en un 403.
 */
const restrictRoles = (
  sections: SidebarSection[],
  roles: string[] | undefined,
): SidebarSection[] =>
  sections
    .map((section) => ({
      ...section,
      items: section.items.filter(
        (item) => !item.roles || item.roles.some((rol) => roles?.includes(rol)),
      ),
    }))
    .filter((section) => section.items.length > 0);

const sidebarSections: SidebarSection[] = [
  {
    title: "General",
    items: [
      {
        label: "Dashboard",
        href: "/dashboard/home",
        icon: <ChartColumn className="w-5 h-5" />,
      },
      {
        label: "Ocupación",
        href: "/dashboard/ocupacion",
        icon: <CalendarCheck className="w-5 h-5" />,
      },
    ],
  },
  {
    title: "Caja",
    items: [
      {
        label: "Caja",
        href: "/dashboard/caja",
        icon: <Wallet className="w-5 h-5" />,
      },
      {
        label: "Pagos",
        href: "/dashboard/pagos",
        icon: <CreditCard className="w-5 h-5" />,
      },
    ],
  },
  {
    title: "Gestión",
    items: [
      {
        label: "Clientes",
        href: "/dashboard/clientes",
        icon: <ClientsIcon className="w-5 h-5" />,
      },
      {
        label: "Reservas",
        href: "/dashboard/reservas",
        icon: <ReservaIcon className="w-5 h-5" />,
      },
      {
        label: "Habitaciones",
        href: "/dashboard/habitaciones",
        icon: <RoomIcon className="w-5 h-5" />,
      },
      {
        label: "Tienda",
        href: "/dashboard/tienda",
        icon: <ShoppingBag className="w-5 h-5" />,
      },
      {
        label: "Empleados",
        href: "/dashboard/empleados",
        icon: <EmpleadoIcon className="w-5 h-5" />,
        roles: ["ADMIN"],
      },
      {
        label: "Usuarios",
        href: "/dashboard/usuarios",
        icon: <UserIcon className="w-5 h-5" />,
        roles: ["ADMIN"],
      },
      {
        label: "PQRS",
        href: "/dashboard/pqrs",
        icon: <FileText className="w-5 h-5" />,
      },
    ],
  },
  {
    title: "Monitoreo",
    items: [
      {
        label: "Registros",
        href: "/dashboard/registros",
        icon: <ClipboardList className="w-5 h-5" />,
      },
      {
        label: "Copias de seguridad",
        href: "/dashboard/backups",
        icon: <DatabaseBackup className="w-5 h-5" />,
        roles: ["ADMIN"],
      },
      {
        // El espacio final del label original es un typo heredado; se conserva para no
        // alterar lo ya publicado, pero se anota aqui.
        label: "Historial de acciones ",
        href: "/dashboard/historial-acciones",
        icon: <History className="w-5 h-5" />,
        roles: ["ADMIN"],
      },
    ],
  },
  {
    title: "Configuración",
    items: [
      {
        label: "Ajustes",
        href: "/dashboard/ajustes",
        icon: <SettingsIcon className="w-5 h-5" />,
      },
    ],
  },
];

const DashboardLayout: React.FC = () => {
  const { user } = useAuth();

  // Sin sesion resuelta no se muestra nada: ante la duda se oculta, no se expone. En la
  // practica ProtectedRoute ya bloquea el render hasta tener `user`, asi que esto es solo
  // una defensa por si el layout se monta aislado en una prueba.
  const secciones = useMemo(
    () => (user ? restrictRoles(sidebarSections, user.roles) : []),
    [user],
  );

  useEffect(() => {
    const html = document.documentElement;
    const body = document.body;
    const prevHtml = html.style.overflow;
    const prevBody = body.style.overflow;
    html.style.overflow = "hidden";
    body.style.overflow = "hidden";
    return () => {
      html.style.overflow = prevHtml;
      body.style.overflow = prevBody;
    };
  }, []);

  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <>
      {/*
        Una sola etiqueta desde el layout cubre las ~40 rutas que cuelgan de
        /dashboard. `Disallow` en robots.txt no impide la indexación: solo
        impide el rastreo, así que una URL de panel encontrada por un enlace
        aparecería en el índice como "Bloqueada por robots.txt". Hace falta el
        noindex explícito.
      */}
      <SEOHead
        title="Panel del hotel"
        description="Gestión interna de reservas, habitaciones y clientes"
        url="/dashboard"
        noindex
      />
      <div className="h-screen flex overflow-hidden">
        <Sidebar
          sections={secciones}
          logo={{ src: Logo, alt: "Hotel Cacique de Bolivar" }}
          isMobileOpen={sidebarOpen}
          onMobileClose={() => setSidebarOpen(false)}
        />

        <div className="flex-1 flex flex-col min-h-0">
          <Topbar onMenuClick={() => setSidebarOpen(true)} />

          <main className="flex-1 overflow-y-auto overflow-x-hidden bg-card min-h-0 min-w-0">
            <Outlet />
          </main>
        </div>
      </div>
    </>
  );
};

export default DashboardLayout;
