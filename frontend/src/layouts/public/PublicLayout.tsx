import type { ReactNode } from "react";
import { useNavigate, Link } from "react-router-dom";
import Navbar from "@/components/layout/Navbar";
import Footer from "@/components/layout/Footer";
import Hero, { type HeroSlide } from "@/layouts/hero/Hero";
import amalfi2 from "@/assets/amalfi-paisaje.jpg";
import panoramica from "@/assets/panoramica.jpg";
import fuente from "@/assets/fuente.jpg";
import parque from "@/assets/parque.jpg";
import fachada2 from "@/assets/hotel-fachada2.webp";
import sala from "@/assets/hotel-sala2.webp";
import pasillo from "@/assets/hotel-pasillo.webp";
import { CircleHelp } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { useScroll } from "@/features/home/hooks/useScroll";
import { useAuth } from "@/app/auth/AuthContext";
import UserMenu from "@/features/home/components/UserMenu";
import LoginButton from "@/features/home/components/LoginButton";
import { usePublicLayout } from "./PublicLayoutContext";

interface PublicLayoutProps {
  children: ReactNode;
  heroContent?: ReactNode;
}

const navLinks = [
  { title: "Habitaciones", href: "/#habitaciones" },
  { title: "Servicios", href: "/#servicios" },
  { title: "Reseñas", href: "/#resenas" },
  { title: "Ubicación", href: "/#ubicacion" },
  { title: "Fiestas", href: "/#fiestas" },
];

/**
 * Solo la fachada lleva alt descriptivo: es la imagen que un motor de búsqueda
 * indexa y la primera que ve quien llega. Las demás quedan como decorativas.
 */
const heroSlides: HeroSlide[] = [
  {
    src: fachada2,
    alt: "Fachada del Hotel Cacique de Bolivar en Amalfi, Antioquia",
  },
  { src: pasillo, alt: "" },
  { src: sala, alt: "" },
  { src: amalfi2, alt: "" },
  { src: panoramica, alt: "" },
  { src: fuente, alt: "" },
  { src: parque, alt: "" },
];

export default function PublicLayout({
  children,
  heroContent,
}: PublicLayoutProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const scrolled = useScroll();
  const { openLogin, openCompleteProfile, openPQRSForm } = usePublicLayout();

  const roles = user?.roles ?? [];
  const isStaff = roles.includes("ADMIN") || roles.includes("RECEPCIONISTA");
  const needsClientProfile = roles.includes("CLIENTE") && !user?.clienteId;

  const handleLogout = async () => {
    await logout();
    navigate("/", { replace: true });
  };

  // Sin hero (ej. /pago) el navbar siempre debe verse sólido (letra oscura + sombra rose) para no quedar invisible sobre fondo claro
  const isSolid = scrolled || !heroContent;
  const helpDropdownTriggerClass = isSolid
    ? "text-stone-600 hover:text-rose-600 hover:bg-rose-50"
    : "text-white/80 hover:text-white hover:bg-white/10";

  const helpDropdown = (
    <DropdownMenu>
      <DropdownMenuTrigger
        className={`flex items-center justify-center size-9 rounded-xl transition ${helpDropdownTriggerClass}`}
        aria-label="Centro de Ayuda">
        <CircleHelp className="size-5" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48 font-nav force-light bg-white text-stone-900 border-stone-200 [color-scheme:light]">
        <DropdownMenuItem asChild>
          <Link to="/ayuda" className="cursor-pointer">
            Centro de Ayuda
          </Link>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={openPQRSForm} className="cursor-pointer">
          Crear PQRS
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link to="/ayuda#contacto" className="cursor-pointer">
            Contacto
          </Link>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );

  const navbarBg = isSolid
    ? "bg-white/90 backdrop-blur-md shadow-[0_12px_18px_-6px_rgba(244,63,94,0.25)]"
    : "bg-transparent";
  const navbarText = isSolid ? "text-stone-800" : "text-white";
  const linkClass = isSolid
    ? "hover:bg-rose-50 hover:text-rose-600"
    : "hover:bg-white/10";
  const mobileClass = isSolid
    ? "bg-white/95 backdrop-blur-md"
    : "bg-stone-900/95 backdrop-blur-md";

  return (
    <div className="force-light min-h-screen bg-stone-50 text-stone-900 [color-scheme:light]">
      <div
        className={`fixed top-0 left-0 w-full z-50 transition-all duration-300 ${navbarBg}`}>
        <Navbar
          links={navLinks}
          afterLinks={helpDropdown}
          mobileLinks={[
            { title: "Centro de Ayuda", href: "/ayuda" },
            { title: "Crear PQRS", href: "/ayuda#pqrs", onClick: openPQRSForm },
            { title: "Contacto", href: "/ayuda#contacto" },
          ]}
          className={navbarText}
          linkClassName={linkClass}
          mobileMenuClassName={mobileClass}
          rightSlot={
            user ? (
              <UserMenu
                user={user}
                scrolled={isSolid}
                isStaff={isStaff}
                needsClientProfile={needsClientProfile}
                onCompleteProfile={openCompleteProfile}
                onLogout={handleLogout}
              />
            ) : (
              <LoginButton scrolled={isSolid} onClick={openLogin} />
            )
          }
        />
      </div>

      {heroContent && (
        <section className="relative w-full h-[70vh] sm:h-[75vh] md:h-[80vh] overflow-hidden group shadow-[0_40px_80px_-25px_rgba(0,0,0,0.4),0_15px_30px_-15px_rgba(0,0,0,0.25)]">
          <Hero slides={heroSlides} interval={5000} />
          {heroContent}
        </section>
      )}

      <main className={heroContent ? undefined : "pt-24"}>{children}</main>

      <Footer />
    </div>
  );
}
