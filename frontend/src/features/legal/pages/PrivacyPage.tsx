import { Shield, FileText, Database, Lock, Cookie, Mail, Scale, Clock, Eye, Share2, RefreshCw } from "lucide-react";
import PublicLayout from "@/layouts/public/PublicLayout";
import { SEOHead, JSONLD, breadcrumbs } from "@/components/seo";
import { HOTEL } from "@/config/hotel";
import LegalHeroContent from "../components/LegalHeroContent";
import PageTOC from "../components/PageTOC";

const sectionIcons: Record<string, typeof Shield> = {
  "1": Shield,
  "2": Scale,
  "3": Database,
  "4": FileText,
  "5": Eye,
  "6": Share2,
  "7": Lock,
  "8": Clock,
  "9": Cookie,
  "10": RefreshCw,
  "11": Mail,
};

interface Section {
  id: string;
  title: string;
  content: string;
  items?: string[];
  footerContent?: string;
}

const sections: Section[] = [
  {
    id: "1",
    title: "Identificación del Responsable",
    content:
      `Hotel Cacique de Bolivar, identificado con NIT ${HOTEL.nit}, con domicilio en la Calle 10 #5-20, Amalfi, Antioquia, Colombia, es el responsable del tratamiento de sus datos personales. Para cualquier consulta relacionada con esta política, puede contactarnos al correo electrónico contacto@example.com o al teléfono ${HOTEL.phoneDisplay}.`,
  },
  {
    id: "2",
    title: "Marco Legal",
    content:
      "Esta política de privacidad se rige por lo dispuesto en la Ley 1581 de 2012, el Decreto 1377 de 2013 y las demás disposiciones complementarias sobre protección de datos personales en la República de Colombia.",
  },
  {
    id: "3",
    title: "Datos Personales que Recopilamos",
    content: "Podemos recopilar la siguiente información personal:",
    items: [
      "Nombres y apellidos",
      "Número de cédula",
      "Dirección de correo electrónico",
      "Número de teléfono",
      "Dirección de residencia",
      "Fecha de nacimiento",
      "Datos de facturación",
      "Información de pago",
      "Historial de reservas",
      "Preferencias de hospedaje",
    ],
    footerContent:
      "Así mismo, recopilamos información de navegación a través de cookies y tecnologías similares.",
  },
  {
    id: "4",
    title: "Finalidades del Tratamiento",
    content: "Sus datos personales serán tratados con las siguientes finalidades:",
    items: [
      "Gestionar y procesar sus reservas de hospedaje",
      "Prestar los servicios hoteleros solicitados",
      "Enviar información sobre promociones y ofertas especiales",
      "Cumplir obligaciones legales y fiscales",
      "Mejorar nuestros servicios y experiencia del cliente",
      "Atender solicitudes, quejas y reclamos",
      "Realizar análisis estadísticos de uso",
    ],
  },
  {
    id: "5",
    title: "Derechos del Titular (ARCO)",
    content: "De conformidad con la Ley 1581 de 2012, usted tiene los siguientes derechos:",
    items: [
      "Acceder a sus datos personales que hayan sido recopilados",
      "Conocer, actualizar y rectificar su información",
      "Solicitar la eliminación de sus datos cuando considere que no están siendo tratados conforme a la ley",
      "Oponerse al tratamiento de sus datos para fines específicos",
      "Solicitar prueba de la autorización otorgada",
    ],
    footerContent:
      "Para ejercer estos derechos, puede enviar una solicitud a nuestro correo electrónico indicando el derecho que desea ejercer.",
  },
  {
    id: "6",
    title: "Transferencia y Transmisión de Datos",
    content:
      "Sus datos personales no serán transferidos a terceros sin su consentimiento previo, salvo las excepciones legales contempladas en la Ley 1581 de 2012. Podremos compartir su información con autoridades competentes cuando sea requerido por ley, y con procesadores de datos que nos presten servicios de infraestructura tecnológica, procesamiento de pagos y envío de comunicaciones, quienes estarán sujetos a las mismas obligaciones de protección de datos.",
  },
  {
    id: "7",
    title: "Seguridad de los Datos",
    content:
      "Hotel Cacique de Bolivar adopta las medidas técnicas, administrativas y humanas necesarias para garantizar la seguridad de sus datos personales y evitar su adulteración, pérdida, consulta, uso o acceso no autorizado. Implementamos protocolos de cifrado, firewalls, controles de acceso y políticas internas de tratamiento de la información.",
  },
  {
    id: "8",
    title: "Periodo de Conservación",
    content:
      "Conservaremos sus datos personales durante el tiempo necesario para cumplir las finalidades descritas en esta política, y posteriormente durante los plazos legales establecidos por la normativa colombiana aplicable. Una vez cumplidos estos plazos, sus datos serán eliminados de forma segura.",
  },
  {
    id: "9",
    title: "Política de Cookies",
    content:
      "Nuestro sitio web utiliza cookies propias y de terceros para mejorar la experiencia de navegación, analizar el tráfico del sitio y personalizar el contenido. Las cookies son pequeños archivos de texto que se almacenan en su navegador. Puede configurar su navegador para rechazar todas las cookies o para indicar cuándo se envía una cookie. Sin embargo, algunas funcionalidades del sitio podrían no funcionar correctamente si desactiva las cookies.",
  },
  {
    id: "10",
    title: "Cambios a esta Política",
    content:
      "Nos reservamos el derecho de modificar esta política de privacidad en cualquier momento. Los cambios serán publicados en esta página y, cuando sea procedente, notificados a través de nuestros canales de comunicación. Le recomendamos revisar periódicamente esta política para estar informado de cómo protegemos su información.",
  },
  {
    id: "11",
    title: "Contacto",
    content:
      "Si tiene preguntas, inquietudes o desea ejercer sus derechos ARCO, puede contactarnos a través de los siguientes canales:",
    items: [
      "Correo electrónico: contacto@example.com",
      "Teléfono: +57 300 0000000",
      "Dirección: Calle 10 #5-20, Amalfi, Antioquia",
    ],
  },
];

export default function PrivacyPage() {
  return (
    <>
      <SEOHead
        title="Política de Privacidad"
        description="Cómo Hotel Cacique de Bolivar, en Amalfi Antioquia, trata y protege sus datos personales conforme a la Ley 1581 de 2012 de Colombia."
        url="/privacidad"
      />
      <JSONLD
        data={breadcrumbs([
          { name: "Inicio", url: "/" },
          { name: "Política de Privacidad", url: "/privacidad" },
        ])}
      />
      <PublicLayout
        heroContent={
          <LegalHeroContent
            icon={Shield}
            title="Política de Privacidad"
            subtitle="Conoce cómo protegemos y tratamos tus datos personales"
          />
        }
      >
        <div className="mx-auto max-w-7xl px-4 md:px-8 py-10 md:py-14">
          <div className="flex flex-col lg:flex-row gap-10 lg:gap-16">
            <PageTOC sections={sections} />

            <div className="flex-1 min-w-0 space-y-3 scroll-smooth">
              {sections.map((section) => {
                const Icon = sectionIcons[section.id] || Shield;
                const isContact = section.id === "11";

                return (
                  <section
                    key={section.id}
                    id={section.id}
                    className={`scroll-mt-32 rounded-xl border p-5 sm:p-6 md:p-7 ${
                      isContact
                        ? "bg-linear-to-br from-rose-50 to-white border-rose-200"
                        : "bg-white border-stone-100"
                    }`}
                  >
                    <div className="flex items-start gap-3 mb-3">
                      <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-rose-100">
                        <Icon className="size-4 text-rose-600" />
                      </span>
                      <h2 className="font-heading text-base font-medium text-stone-800 leading-snug pt-0.5">
                        {section.id}. {section.title}
                      </h2>
                    </div>
                    <div className="ml-11 space-y-3">
                      <p className="text-stone-600 leading-relaxed font-sans text-[15px] break-words">
                        {section.content}
                      </p>
                      {section.items && (
                        <ul className="space-y-1.5">
                          {section.items.map((item, i) => (
                            <li
                              key={i}
                              className="flex items-start gap-2.5 text-stone-600 text-[15px] leading-relaxed"
                            >
                              <span className="mt-2 size-1.5 shrink-0 rounded-full bg-rose-400" />
                              <span className="break-words">{item}</span>
                            </li>
                          ))}
                        </ul>
                      )}
                      {section.footerContent && (
                        <p className="text-stone-600 leading-relaxed font-sans text-[15px] break-words">
                          {section.footerContent}
                        </p>
                      )}
                    </div>
                  </section>
                );
              })}
            </div>
          </div>
        </div>
      </PublicLayout>
    </>
  );
}
