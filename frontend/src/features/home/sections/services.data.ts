import { Wifi, Tv, Bath, Clock, Sparkles, Shirt, Shield } from "lucide-react";

/**
 * Servicios del hotel. Se exportan aparte del componente porque el schema.org
 * de la portada los reutiliza en `amenityFeature`, y un archivo que exporta a la
 * vez componentes y datos rompe el fast refresh de React.
 */
export const services = [
  {
    icon: Wifi,
    title: "WiFi Gratis",
    description:
      "Conexión inalámbrica de alta velocidad en todas las habitaciones y áreas del hotel.",
  },
  {
    icon: Bath,
    title: "Ducha Agua Caliente",
    description:
      "Agua caliente disponible las 24 horas para tu confort y relajación.",
  },
  {
    icon: Clock,
    title: "Recepción 24 hrs",
    description:
      "Atención personalizada todos los días, en cualquier momento que lo necesites.",
  },
  {
    icon: Tv,
    title: "TV Pantalla Plana",
    description:
      "Televisor de pantalla plana con señal por cable en cada habitación.",
  },
  {
    icon: Sparkles,
    title: "Servicio de Limpieza",
    description:
      "Limpieza diaria de la habitación para que disfrutes siempre un ambiente fresco.",
  },
  {
    icon: Shirt,
    title: "Servicio de Lavandería",
    description: "Lavandería profesional disponible con costo adicional.",
    highlight: "Costo adicional",
  },
  {
    icon: Shield,
    title: "Seguridad 24/7",
    description:
      "Personal de vigilancia y cámaras para tu tranquilidad durante toda tu estancia.",
  },
];
