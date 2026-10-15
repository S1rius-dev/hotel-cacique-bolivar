import { useState } from "react";
import logo from "@/assets/logo.png";
import { Link } from "react-router-dom";

interface NavLink {
  title: string;
  href: string;
  onClick?: () => void;
}

interface NavbarProps {
  links: NavLink[];

  /** Elemento derecho (login, perfil, botón, lo que sea) */
  rightSlot?: React.ReactNode;

  /** Elemento opcional entre los links y rightSlot (solo desktop) */
  afterLinks?: React.ReactNode;

  /** Links extra para el menú mobile (se renderizan como <a> debajo de links) */
  mobileLinks?: NavLink[];

  /** Clases para colores (text-black, bg-white, etc.) */
  className?: string;

  /** Clases para links */
  linkClassName?: string;

  /** Clases para el contenedor móvil */
  mobileMenuClassName?: string;
}

function Navbar({
  links,
  rightSlot,
  afterLinks,
  mobileLinks,
  className = "",
  linkClassName = "",
  mobileMenuClassName = "",
}: NavbarProps) {
  const [open, setOpen] = useState(false);

  return (
    <div className="px-5 py-1.5">
      <nav
        className={`
          flex items-center justify-between
          relative z-20
          ${className}
        `}>
        {/* LOGO (estructura, no color) */}

        <div className="flex items-center gap-2 hover:scale-105 transition">
          <Link
            to="/"
            className="flex items-center gap-2 hover:scale-105 transition">
            <img
              src={logo}
              alt="Hotel logo"
              className="w-30 h-20 rounded-xl cursor-pointer"
            />
            <span className="py-1 text-lg font-heading font-semibold">
              Hotel Cacique de Bolivar
            </span>
          </Link>
        </div>

        {/* HAMBURGUESA */}
        <button
          onClick={() => setOpen(!open)}
          className="md:hidden text-xl pr-2">
          {open ? "✕" : "☰"}
        </button>

        {/* DESKTOP */}
        <div className="hidden md:flex items-center gap-6">
          <ul className="flex gap-4 font-nav text-base font-medium">
            {links.map((link) => (
              <li key={`${link.href}-${link.title}`}>
                <a
                  href={link.href}
                  className={`px-3 py-1 rounded-xl transition ${linkClassName}`}>
                  {link.title}
                </a>
              </li>
            ))}
          </ul>

          {/* AFTER LINKS */}
          {afterLinks}

          {/* SLOT DERECHO */}
          {rightSlot}
        </div>

        {/* MOBILE */}
        {open && (
          <div
            className={`
              absolute top-full left-0 w-full
              md:hidden flex flex-col p-5 gap-4
              ${mobileMenuClassName}
            `}>
            {links.map((link) => (
              <a
                key={`${link.href}-${link.title}`}
                href={link.href}
                onClick={() => setOpen(false)}
                className={`px-3 py-2 rounded-lg transition font-nav ${linkClassName}`}>
                {link.title}
              </a>
            ))}
            {mobileLinks?.map((link) =>
              link.onClick ? (
                <button
                  key={`${link.href}-${link.title}`}
                  onClick={() => {
                    setOpen(false);
                    link.onClick?.();
                  }}
                  className={`px-3 py-2 rounded-lg transition font-nav text-left w-full ${linkClassName}`}>
                  {link.title}
                </button>
              ) : (
                <a
                  key={`${link.href}-${link.title}`}
                  href={link.href}
                  onClick={() => setOpen(false)}
                  className={`px-3 py-2 rounded-lg transition font-nav ${linkClassName}`}>
                  {link.title}
                </a>
              ),
            )}

            <hr className="border-t border-white/10 my-1" />

            <div className="self-start">{rightSlot}</div>
          </div>
        )}
      </nav>
    </div>
  );
}

export default Navbar;
