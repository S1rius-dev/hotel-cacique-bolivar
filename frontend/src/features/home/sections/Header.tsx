import { useAuth } from "@/app/auth/AuthContext";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/layout/Navbar";
import { useScroll } from "../hooks/useScroll";
import UserMenu from "../components/UserMenu";
import LoginButton from "../components/LoginButton";
import HeroSection from "../components/HeroSection";

interface HeaderProps {
  onLoginClick: () => void;
  onCompleteProfile?: () => void;
  hideHero?: boolean;
  scrollThreshold?: number;
  scrolledVariant?: "light" | "dark";
}

export default function Header({
  onLoginClick,
  onCompleteProfile,
  hideHero,
  scrollThreshold,
  scrolledVariant = "light",
}: HeaderProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const scrolled = useScroll(scrollThreshold);
  const roles = user?.roles ?? [];
  const isStaff = roles.includes("ADMIN") || roles.includes("RECEPCIONISTA");
  const needsClientProfile = roles.includes("CLIENTE") && !user?.clienteId;

  const handleLogout = async () => {
    await logout();
    navigate("/", { replace: true });
  };

  const scrolledBg =
    scrolledVariant === "dark"
      ? "bg-stone-900/90 backdrop-blur-md shadow-[0_12px_18px_-6px_rgba(0,0,0,0.4)]"
      : "bg-white/90 backdrop-blur-md shadow-[0_12px_18px_-6px_rgba(244,63,94,0.25)]";

  const textColor = scrolled && scrolledVariant === "light" ? "text-stone-800" : "text-white";

  return (
    <>
      <div
        className={`
          fixed top-0 left-0 w-full z-50 transition-all duration-300
          ${scrolled ? scrolledBg : "bg-transparent"}
        `}
      >
        <Navbar
          links={[
            { title: "Habitaciones", href: "#habitaciones" },
            { title: "Servicios", href: "#servicios" },
            { title: "Reseñas", href: "#resenas" },
            { title: "Ubicación", href: "#ubicacion" },
          ]}
          className={textColor}
          linkClassName={
            scrolled && scrolledVariant === "light"
              ? "hover:bg-rose-50 hover:text-rose-600"
              : "hover:bg-white/10"
          }
          mobileMenuClassName={
            scrolled && scrolledVariant === "light"
              ? "bg-white/95 backdrop-blur-md"
              : "bg-stone-900/95 backdrop-blur-md"
          }
          rightSlot={
            user ? (
              <UserMenu
                user={user}
                scrolled={scrolled}
                isStaff={isStaff}
                needsClientProfile={needsClientProfile}
                onCompleteProfile={() => onCompleteProfile?.()}
                onLogout={handleLogout}
              />
            ) : (
              <LoginButton scrolled={scrolled} onClick={onLoginClick} />
            )
          }
        />
      </div>

      {!hideHero && <HeroSection />}
    </>
  );
}
