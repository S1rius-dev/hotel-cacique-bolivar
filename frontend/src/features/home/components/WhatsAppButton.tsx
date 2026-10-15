import { FaWhatsapp } from "react-icons/fa";

export default function WhatsAppButton() {
  return (
    <a
      href="https://wa.me/573000000000"
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Contactar por WhatsApp"
      className="
        fixed
        bottom-4 sm:bottom-5 md:bottom-10
        right-4 sm:right-5 md:right-6
        z-50
        flex h-12 w-12 sm:h-14 sm:w-14
        items-center justify-center
        rounded-full
        bg-green-500 text-white
        shadow-[0_10px_25px_-5px_rgba(34,197,94,0.5)]
        transition-all duration-300
        hover:scale-110 hover:bg-green-400
        hover:shadow-[0_15px_35px_-5px_rgba(34,197,94,0.6)]
      ">
      <FaWhatsapp className="w-8 h-8 sm:w-10 sm:h-10" />
    </a>
  );
}
