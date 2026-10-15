import {
  HelpCircle,
  CalendarDays,
  Clock,
  Sparkles,
  MapPin,
  CreditCard,
  RotateCcw,
  MessageCircle,
  PartyPopper,
  Phone,
  Mail,
  ChevronDown,
  MessageSquareText,
  ArrowRight,
} from "lucide-react";
import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import PublicLayout from "@/layouts/public/PublicLayout";
import { usePublicLayout } from "@/layouts/public/PublicLayoutContext";
import { SEOHead, JSONLD, breadcrumbs } from "@/components/seo";
import LegalHeroContent from "../components/LegalHeroContent";

const categoryIcons: Record<string, typeof HelpCircle> = {
  Reservas: CalendarDays,
  "Check-in y Check-out": Clock,
  "Servicios y Amenidades": Sparkles,
  "Turismo y fiestas en Amalfi": PartyPopper,
  "Ubicación y Transporte": MapPin,
  "Pagos y Facturación": CreditCard,
  "Cancelaciones y Reembolsos": RotateCcw,
};

const faqs = [
  {
    category: "Reservas",
    items: [
      {
        q: "¿Cómo puedo hacer una reserva?",
        a: "Puede hacer su reserva directamente en nuestro sitio web utilizando el buscador de reservas, llamando a nuestra recepción al +57 300 0000000, o enviando un mensaje a través de WhatsApp. También puede visitarnos personalmente en nuestra dirección: Calle 10 #5-20, Amalfi, Antioquia.",
      },
      {
        q: "¿Necesito una tarjeta de crédito para reservar?",
        a: "No es obligatorio. Aceptamos reservas con tarjeta de crédito, transferencia bancaria o depósito en efectivo. Para temporada alta y eventos especiales, podríamos solicitar un abono del 50% como anticipo.",
      },
      {
        q: "¿Puedo modificar mi reserva?",
        a: "Sí, puede modificar su reserva siempre que haya disponibilidad. Las modificaciones están sujetas a cambios en la tarifa. Le recomendamos contactarnos con al menos 48 horas de anticipación para garantizar los cambios solicitados.",
      },
      {
        q: "¿Cómo sé que mi reserva está confirmada?",
        a: "Una vez realizada su reserva, recibirá un correo electrónico de confirmación con todos los detalles de su estadía. Si no recibe esta confirmación en un plazo de 24 horas, por favor contáctenos.",
      },
    ],
  },
  {
    category: "Check-in y Check-out",
    items: [
      {
        q: "¿Cuál es el horario de check-in?",
        a: "El check-in está disponible durante todo el día. Si llega antes, puede dejar su equipaje en recepción mientras espera que su habitación esté lista.",
      },
      {
        q: "¿Cuál es el horario de check-out?",
        a: "El check-out debe realizarse antes de la 1 P.M. Si necesita salir más tarde, pregunte en recepción por la disponibilidad de late check-out (sujeto a cargos adicionales).",
      },
      {
        q: "¿Qué documentos necesito para el check-in?",
        a: "Debe presentar un documento de identidad válido (cédula de ciudadanía). En el caso de menores de edad, deben presentar su registro civil o tarjeta de identidad, y estar acompañados por sus padres o tutores.",
      },
    ],
  },
  {
    category: "Servicios y Amenidades",
    items: [
      {
        q: "¿El hotel ofrece desayuno?",
        a: "No, no ofrecemos desayuno incluido en las tarifas.",
      },
      {
        q: "¿El hotel tiene parqueadero privado?",
        a: "No. El Hotel Cacique de Bolivar no cuenta con parqueadero propio. Si llega en vehículo, escríbanos al +57 300 0000000 y le orientamos sobre las opciones de parqueo disponibles en el sector y en el casco urbano de Amalfi.",
      },
      {
        q: "¿El hotel tiene acceso a internet?",
        a: "Sí, ofrecemos WiFi gratuito en todas nuestras instalaciones, incluyendo habitaciones y áreas comunes.",
      },
      {
        q: "¿Aceptan mascotas?",
        a: "Por favor consulte con nuestro equipo de recepción sobre nuestra política de mascotas, ya que está sujeta a disponibilidad y condiciones específicas.",
      },
      {
        q: "¿Tiene servicio de lavandería?",
        a: "Si, ofrecemos servicio de lavandería para nuestros huéspedes. Por favor consulte en recepción para más detalles sobre horarios y tarifas.",
      },
    ],
  },
  {
    category: "Turismo y fiestas en Amalfi",
    items: [
      {
        q: "¿Qué puedo hacer en Amalfi, Antioquia?",
        a: "Amalfi es conocida como la Ciudad Señora del Nordeste antioqueño. El parque principal y el trazado de sus calles, obra del ingeniero Carlos Segismundo de Greiff, concentran buena parte del interés. En los alrededores se encuentran el río Riachón, los embalses de Porce II y Porce III, y el paisaje de montaña con cascadas. Desde el hotel puede recorrer todo el casco urbano caminando.",
      },
      {
        q: "¿Hay habitaciones disponibles durante las fiestas de Amalfi?",
        a: "Las Fiestas de la Paz y el Progreso, en octubre, son la celebración más representativa del municipio y atraen visitantes de toda la región. La disponibilidad se agota rápido en esas fechas. Le recomendamos reservar con anticipación o escribirnos por WhatsApp al +57 300 0000000 para consultar disponibilidad y tarifas.",
      },
      {
        q: "¿Qué otras fiestas se celebran en Amalfi?",
        a: "El calendario tradicional incluye las Fiestas de los Reyes Magos en enero, las Fiestas del Campesino del 2 al 4 de junio, las Fiestas de la Virgen del Carmen a mediados de julio, el Festival del Río y el Cooperativismo el 20 de julio, las Fiestas de la Paz y el Progreso en octubre y las Fiestas de la Inmaculada Concepción el 8 de diciembre. Las fechas pueden cambiar en cada edición: confírmelas con la Alcaldía de Amalfi antes de viajar.",
      },
    ],
  },
  {
    category: "Ubicación y Transporte",
    items: [
      {
        q: "¿Dónde está ubicado el hotel?",
        a: "Estamos ubicados en la Calle 10 #5-20, en el centro de Amalfi, Antioquia, a menos de una cuadra del parque principal. Nuestra ubicación privilegiada le permite acceder caminando a los principales atractivos turísticos y comerciales del municipio.",
      },
      {
        q: "¿Cómo llego a Amalfi, Antioquia?",
        a: "Desde Medellín son 151 kilómetros, unas tres horas por la Autopista Norte y luego la vía del Nordeste. En transporte público, la empresa Coonorte despacha desde la Terminal del Norte de Medellín, cerca de la estación Caribe del Metro, y llega hasta Amalfi. Si maneja vehículo propio, se recomienda viajar de día por el carácter montañoso de la vía.",
      },
      {
        q: "¿Ofrecen servicio de transporte?",
        a: "Podemos ayudarle a coordinar su transporte desde y hacia el hotel. Contáctenos con anticipación para organizar el servicio.",
      },
    ],
  },
  {
    category: "Pagos y Facturación",
    items: [
      {
        q: "¿Qué métodos de pago aceptan?",
        a: "Aceptamos pagos en efectivo (pesos colombianos), transferencias bancarias, y tarjetas de crédito/débito. Las tarifas incluyen IVA y demás impuestos aplicables.",
      },
      {
        q: "¿Puedo obtener una factura?",
        a: "Sí, emitimos factura electrónica con todos los requisitos fiscales colombianos. Por favor solicítela en recepción indicando los datos de facturación (NIT/RUT, razón social).",
      },
    ],
  },
  {
    category: "Cancelaciones y Reembolsos",
    items: [
      {
        q: "¿Cuál es la política de cancelación?",
        a: "Puede cancelar sin costo hasta 48 horas antes de su llegada. Si cancela dentro de las 48 horas previas, podría aplicarse el cobro de una noche. En caso de no presentarse, se cobrará la totalidad de la reserva.",
      },
      {
        q: "¿Cómo solicito un reembolso?",
        a: "Para solicitar un reembolso, debe contactarnos a través de nuestro correo contacto@example.com o llamando al +57 300 0000000. Los reembolsos se procesarán dentro de los 15 días hábiles siguientes a la solicitud.",
      },
    ],
  },
];

function FaqItem({ q, a }: { q: string; a: string }) {
  const [open, setOpen] = useState(false);
  const slug = q.slice(0, 32).replace(/\W+/g, "-").toLowerCase();
  const buttonId = `faq-${slug}`;
  const panelId = `faq-panel-${slug}`;

  return (
    <div className="border border-stone-200 rounded-xl overflow-hidden transition-all duration-200 hover:border-rose-200">
      <button
        id={buttonId}
        type="button"
        onClick={() => setOpen(!open)}
        aria-expanded={open}
        aria-controls={panelId}
        className="w-full flex items-center justify-between gap-4 px-5 py-4 text-left cursor-pointer">
        <span className="text-sm font-medium text-stone-700 leading-snug pr-4">
          {q}
        </span>
        <ChevronDown
          className={`size-4 shrink-0 text-rose-400 transition-transform duration-200 ${open ? "rotate-180" : ""}`}
        />
      </button>
      {/*
        La respuesta se monta siempre y se oculta con grid-rows, en lugar de
        condicionarse con `open &&`. Antes no existía en el DOM hasta que se
        abría, pero el JSON-LD FAQPage de más abajo sí la declaraba: es
        contenido marcado como visible que en realidad no lo estaba.
      */}
      <div
        id={panelId}
        role="region"
        aria-labelledby={buttonId}
        className="grid transition-[grid-template-rows] duration-300 ease-out"
        style={{ gridTemplateRows: open ? "1fr" : "0fr" }}>
        <div className="overflow-hidden">
          <div className="px-5 pb-4">
            <p className="text-sm text-stone-500 leading-relaxed">{a}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

const faqSchema = {
  "@context": "https://schema.org",
  "@type": "FAQPage",
  mainEntity: faqs.flatMap((category) =>
    category.items.map((item) => ({
      "@type": "Question",
      name: item.q,
      acceptedAnswer: {
        "@type": "Answer",
        text: item.a,
      },
    })),
  ),
};

export default function HelpPage() {
  const { hash } = useLocation();
  const { openPQRSForm } = usePublicLayout();

  useEffect(() => {
    if (hash) {
      const el = document.getElementById(hash.replace("#", ""));
      if (el) {
        const y = el.getBoundingClientRect().top + window.scrollY - 96;
        window.scrollTo({ top: y, behavior: "smooth" });
      }
    }
  }, [hash]);

  return (
    <>
      <SEOHead
        title="Preguntas frecuentes sobre tu estadía en Amalfi"
        description="Resuelve tus dudas sobre reservas, check-in, servicios, cómo llegar a Amalfi Antioquia y qué hacer durante tu estadía en el Hotel Cacique de Bolivar."
        url="/ayuda"
      />
      <JSONLD
        data={breadcrumbs([
          { name: "Inicio", url: "/" },
          { name: "Centro de Ayuda", url: "/ayuda" },
        ])}
      />
      <JSONLD data={faqSchema} />
      <PublicLayout
        heroContent={
          <LegalHeroContent
            icon={HelpCircle}
            title="Centro de Ayuda"
            subtitle="Respuestas a las preguntas más frecuentes sobre nuestros servicios"
          />
        }>
        <div className="mx-auto max-w-7xl px-4 md:px-8 py-12 md:py-16">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-10">
            {faqs.map((category) => {
              const Icon = categoryIcons[category.category] || HelpCircle;
              return (
                <section key={category.category}>
                  <h2 className="flex items-center gap-3 font-heading text-lg font-medium text-stone-800 mb-4">
                    <span className="flex size-7 items-center justify-center rounded-lg bg-rose-100 shrink-0">
                      <Icon className="size-3.5 text-rose-600" />
                    </span>
                    {category.category}
                  </h2>
                  <div className="space-y-2.5">
                    {category.items.map((item, idx) => (
                      <FaqItem key={idx} q={item.q} a={item.a} />
                    ))}
                  </div>
                </section>
              );
            })}
          </div>
        </div>

        <div className="mx-auto max-w-7xl px-4 md:px-8 pb-12 md:pb-16">
          <div
            className="border-t border-stone-200 pt-12 scroll-mt-24"
            id="contacto">
            <div className="relative overflow-hidden rounded-2xl border border-rose-200 bg-linear-to-br from-rose-50 to-white p-8 md:p-10 shadow-sm">
              <div
                aria-hidden
                className="absolute -top-10 -right-10 size-40 bg-rose-200/30 rounded-full blur-3xl pointer-events-none"
              />
              <div className="relative">
                <div className="flex items-center gap-3 mb-1">
                  <MessageCircle className="size-5 text-rose-500 shrink-0" />
                  <h2 className="font-heading text-xl font-medium text-stone-800">
                    ¿No encontraste lo que buscabas?
                  </h2>
                </div>
                <p className="text-stone-500 text-sm mb-6">
                  Estamos aquí para ayudarte. Elige la opción que prefieras:
                </p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-3">
                    <p className="text-xs font-semibold text-stone-400 uppercase tracking-wider">
                      Contáctanos directamente
                    </p>
                    <a
                      href="tel:+573000000000"
                      className="flex items-center gap-3 p-4 rounded-xl bg-white border border-stone-200 hover:border-rose-300 hover:shadow-sm transition-all">
                      <Phone className="size-5 text-rose-500 shrink-0" />
                      <div>
                        <p className="text-xs text-stone-400">Teléfono</p>
                        <p className="text-sm font-medium text-stone-700">
                          +57 300 0000000
                        </p>
                      </div>
                    </a>
                    <a
                      href="mailto:contacto@example.com"
                      className="flex items-center gap-3 p-4 rounded-xl bg-white border border-stone-200 hover:border-rose-300 hover:shadow-sm transition-all">
                      <Mail className="size-5 text-rose-500 shrink-0" />
                      <div>
                        <p className="text-xs text-stone-400">Email</p>
                        <p className="text-sm font-medium text-stone-700 truncate">
                          contacto@example.com
                        </p>
                      </div>
                    </a>
                    <a
                      href="https://wa.me/573000000000"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-3 p-4 rounded-xl bg-white border border-stone-200 hover:border-rose-300 hover:shadow-sm transition-all">
                      <MessageCircle className="size-5 text-rose-500 shrink-0" />
                      <div>
                        <p className="text-xs text-stone-400">WhatsApp</p>
                        <p className="text-sm font-medium text-stone-700">
                          Escríbenos
                        </p>
                      </div>
                    </a>
                  </div>
                  <div className="flex flex-col justify-center p-6 rounded-xl bg-rose-50/60 border border-rose-100">
                    <span className="inline-flex self-start items-center gap-1.5 rounded-full bg-rose-100 px-3 py-1 text-xs font-medium text-rose-700 mb-3">
                      <MessageSquareText className="size-3.5" />
                      PQRS
                    </span>
                    <h3 className="font-heading text-base font-semibold text-stone-800 mb-1">
                      ¿Prefieres abrir una PQRS?
                    </h3>
                    <p className="text-sm text-stone-500 mb-4">
                      Crea una petición, queja, reclamo o sugerencia y te
                      responderemos en máximo 48 horas hábiles.
                    </p>
                    <button
                      type="button"
                      onClick={openPQRSForm}
                      className="inline-flex items-center gap-2 rounded-xl border border-rose-800 bg-rose-800 px-6 py-2.5 text-sm font-medium text-white transition-all duration-200 hover:bg-rose-900 hover:shadow-md active:translate-y-px cursor-pointer">
                      Crear PQRS
                      <ArrowRight className="size-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </PublicLayout>
    </>
  );
}
