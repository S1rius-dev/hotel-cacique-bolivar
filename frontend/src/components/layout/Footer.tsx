import { Link } from "react-router-dom";
import { MapPin, Phone, Mail } from "lucide-react";
import { FaFacebookF, FaWhatsapp } from "react-icons/fa";
import logo from "@/assets/logo.png";
import { HOTEL, PROFILES, ADDRESS_LINE } from "@/config/hotel";

/**
 * Rutas completas y no anclas sueltas: en `/reservar`, `/ayuda`, `/terminos` y
 * `/privacidad` un `#habitaciones` apunta a un fragmento que no existe en esa
 * página y el enlace queda roto.
 */
const navigation = [
  { label: "Habitaciones", href: "/#habitaciones" },
  { label: "Servicios", href: "/#servicios" },
  { label: "Reseñas", href: "/#resenas" },
  { label: "Ubicación", href: "/#ubicacion" },
  { label: "Fiestas", href: "/#fiestas" },
];

/**
 * Solo se renderizan los perfiles disponibles. `PROFILES.facebook` es
 * `undefined` en esta demo (el perfil real se retiró), así que se filtra para
 * no dejar un icono con enlace roto.
 */
const socials = [
  PROFILES.facebook
    ? { icon: FaFacebookF, href: PROFILES.facebook, label: "Facebook" }
    : null,
  { icon: FaWhatsapp, href: HOTEL.whatsapp, label: "WhatsApp" },
].filter((social): social is NonNullable<typeof social> => social !== null);

const currentYear = new Date().getFullYear();

export default function Footer() {
  return (
    <footer className="bg-stone-950 text-stone-300">
      <div className="mx-auto max-w-[90rem] px-6 py-12 md:py-14">
        <div className="grid grid-cols-1 gap-12 sm:grid-cols-2 lg:grid-cols-4">
          {/* BRAND */}
          <div className="space-y-4">
            <Link
              to="/"
              className="flex items-center gap-3 hover:opacity-90 transition-opacity">
              <img
                src={logo}
                alt="Hotel Cacique de Bolivar"
                className="h-10 w-auto rounded-lg"
              />
              <span className="font-heading text-lg font-semibold tracking-tight text-stone-100">
                Hotel Cacique
                <br />
                de Bolivar
              </span>
            </Link>
            <p className="text-sm leading-relaxed text-stone-400">
              Un refugio de tradición y confort en el corazón de Colombia. Vive
              una experiencia única con cada visita.
            </p>
          </div>

          {/* NAVEGACIÓN */}
          <div>
            <h3 className="mb-5 text-xs font-semibold uppercase tracking-[0.2em] text-amber-500">
              Navegación
            </h3>
            <ul className="space-y-3">
              {navigation.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-sm text-stone-400 hover:text-amber-400 transition-colors duration-200">
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* CONTACTO */}
          <div>
            <h3 className="mb-5 text-xs font-semibold uppercase tracking-[0.2em] text-amber-500">
              Contacto
            </h3>
            {/*
              <address> no es decorativo: le dice al buscador que la dirección,
              el teléfono y el correo corresponden a un negocio real en ese
              lugar. Con la lista suelta esa correspondencia no era legible.
            */}
            <address className="not-italic space-y-4">
              <p className="flex items-start gap-3 text-sm text-stone-400">
                <MapPin className="mt-0.5 size-4 shrink-0 text-amber-500/80" />
                <span>{ADDRESS_LINE}</span>
              </p>
              <p className="flex items-start gap-3 text-sm text-stone-400">
                <Phone className="mt-0.5 size-4 shrink-0 text-amber-500/80" />
                <a
                  href={`tel:${HOTEL.phone}`}
                  className="hover:text-amber-400 transition-colors duration-200">
                  {HOTEL.phoneDisplay}
                </a>
              </p>
              <p className="flex items-start gap-3 text-sm text-stone-400">
                <Mail className="mt-0.5 size-4 shrink-0 text-amber-500/80" />
                <a
                  href={`mailto:${HOTEL.email}`}
                  className="hover:text-amber-400 transition-colors duration-200">
                  {HOTEL.email}
                </a>
              </p>
            </address>
          </div>

          {/* REDES */}
          <div>
            <h3 className="mb-5 text-xs font-semibold uppercase tracking-[0.2em] text-amber-500">
              Síguenos
            </h3>
            <div className="flex gap-3">
              {socials.map((social) => (
                <a
                  key={social.label}
                  href={social.href}
                  aria-label={social.label}
                  className="flex size-10 items-center justify-center rounded-full border border-stone-700 text-stone-400 hover:border-amber-500 hover:text-amber-400 hover:-translate-y-0.5 transition-all duration-200">
                  <social.icon className="size-4" />
                </a>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* BOTTOM BAR */}
      <div className="border-t border-stone-800">
        <div className="mx-auto flex max-w-[90rem] flex-col items-center justify-between gap-2 px-6 py-5 text-xs text-stone-500 sm:flex-row">
          <p>
            © {currentYear} Hotel Cacique de Bolivar. Todos los derechos
            reservados.
          </p>

          <p>
            Desarrollado por{" "}
            <a
              href="https://www.linkedin.com/in/juan-orrego-ba9946429/"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:underline">
              Juan Orrego
            </a>
          </p>
          <div className="flex gap-5">
            <Link
              to="/privacidad"
              className="hover:text-amber-400 transition-colors">
              Privacidad
            </Link>
            <Link
              to="/terminos"
              className="hover:text-amber-400 transition-colors">
              Términos
            </Link>
            <Link
              to="/ayuda"
              className="hover:text-amber-400 transition-colors">
              Ayuda
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
