import { FileText, UserCheck, CalendarDays, DollarSign, XCircle, Clock, Shield, Users, Baby, Copyright, RefreshCw, Gavel } from "lucide-react";
import PublicLayout from "@/layouts/public/PublicLayout";
import { SEOHead, JSONLD, breadcrumbs } from "@/components/seo";
import { HOTEL } from "@/config/hotel";
import LegalHeroContent from "../components/LegalHeroContent";
import PageTOC from "../components/PageTOC";

const sectionIcons: Record<string, typeof FileText> = {
  "1": FileText,
  "2": UserCheck,
  "3": CalendarDays,
  "4": DollarSign,
  "5": XCircle,
  "6": Clock,
  "7": Shield,
  "8": Users,
  "9": Baby,
  "10": Copyright,
  "11": RefreshCw,
  "12": Gavel,
};

interface Section {
  id: string;
  title: string;
  content: string;
}

const sections: Section[] = [
  {
    id: "1",
    title: "Información General",
    content:
      `Los presentes Términos y Condiciones regulan el uso del sitio web y los servicios de hospedaje ofrecidos por Hotel Cacique de Bolivar, identificado con NIT ${HOTEL.nit}, con domicilio en la Calle 10 #5-20, Amalfi, Antioquia, Colombia. Al realizar una reserva o utilizar nuestros servicios, usted acepta los términos aquí descritos.`,
  },
  {
    id: "2",
    title: "Capacidad Legal",
    content:
      "Los servicios de hospedaje están disponibles para personas mayores de edad con capacidad legal para contratar. Las personas jurídicas podrán realizar reservas a través de sus representantes legales debidamente acreditados. Al aceptar estos términos, declara que tiene la capacidad legal necesaria para obligarse.",
  },
  {
    id: "3",
    title: "Proceso de Reserva",
    content:
      "Las reservas pueden realizarse a través de nuestro sitio web, por vía telefónica o directamente en nuestras instalaciones. Una vez realizada la reserva, el cliente recibirá una confirmación con los detalles de su estadía. La reserva se considera confirmada únicamente cuando el cliente recibe dicha confirmación por parte del hotel.",
  },
  {
    id: "4",
    title: "Tarifas y Formas de Pago",
    content:
      "Las tarifas publicadas están expresadas en pesos colombianos (COP) e incluyen los impuestos aplicables (IVA, impuesto de alojamiento). Aceptamos pagos en efectivo, transferencias bancarias y tarjetas de crédito/débito. Las tarifas están sujetas a cambios sin previo aviso, salvo aquellas confirmadas en una reserva vigente.",
  },
  {
    id: "5",
    title: "Política de Cancelación",
    content:
      "Las cancelaciones realizadas con más de 48 horas de anticipación a la fecha de llegada no tendrán ningún cargo. Las cancelaciones realizadas dentro de las 48 horas previas a la llegada podrán estar sujetas al cobro de una noche de alojamiento. En caso de no presentarse (no-show), se cobrará la totalidad de la reserva. Excepciones pueden aplicarse en temporada alta y eventos especiales.",
  },
  {
    id: "6",
    title: "Check-in y Check-out",
    content:
      "El horario de check-in es a partir de las 15:00 horas y el check-out debe realizarse antes de las 13:00 horas. La solicitud de early check-in o late check-out está sujeta a disponibilidad y podría generar cargos adicionales. Se requiere la presentación de un documento de identidad válido al momento del check-in.",
  },
  {
    id: "7",
    title: "Responsabilidad del Hotel",
    content:
      "Hotel Cacique de Bolivar no se hace responsable por pérdidas o daños de objetos de valor dejados en las habitaciones. El hotel cuenta con caja de seguridad en recepción para el resguardo de sus pertenencias. Tampoco se responsabiliza por daños causados por caso fortuito o fuerza mayor, incluyendo desastres naturales, conflictos sociales o actos de autoridad.",
  },
  {
    id: "8",
    title: "Responsabilidad del Huésped",
    content:
      "El huésped se compromete a hacer uso adecuado de las instalaciones y mobiliario del hotel. Cualquier daño causado a las instalaciones durante su estadía será responsabilidad del huésped, quien deberá asumir el costo de reparación o reposición. El huésped deberá respetar las normas de convivencia y los horarios establecidos por el hotel.",
  },
  {
    id: "9",
    title: "Política de Menores de Edad",
    content:
      "Los menores de edad deben estar acompañados por sus padres o tutores legales durante toda su estadía. El hotel no se hace responsable por la supervisión de menores no acompañados. Los padres o tutores asumen toda la responsabilidad por los actos de los menores a su cargo.",
  },
  {
    id: "10",
    title: "Propiedad Intelectual",
    content:
      "Todo el contenido del sitio web, incluyendo textos, imágenes, logotipos y diseños, es propiedad de Hotel Cacique de Bolivar o de sus licenciantes y está protegido por las leyes de propiedad intelectual de Colombia. Queda prohibida su reproducción, distribución o modificación sin autorización expresa.",
  },
  {
    id: "11",
    title: "Modificaciones",
    content:
      "Hotel Cacique de Bolivar se reserva el derecho de modificar estos términos y condiciones en cualquier momento. Las modificaciones entrarán en vigor inmediatamente después de su publicación en el sitio web. El uso continuado de nuestros servicios después de cualquier modificación constituye la aceptación de los nuevos términos.",
  },
  {
    id: "12",
    title: "Legislación Aplicable",
    content:
      "Estos términos y condiciones se rigen por las leyes de la República de Colombia. Cualquier controversia que surja en relación con estos términos será sometida a la jurisdicción de los tribunales competentes del municipio de Amalfi, Antioquia.",
  },
];

export default function TermsPage() {
  return (
    <>
      <SEOHead
        title="Términos y Condiciones del hospedaje"
        description="Términos y condiciones de reserva y hospedaje del Hotel Cacique de Bolivar en Amalfi, Antioquia. Tarifas, cancelación, check-in y política de menores."
        url="/terminos"
      />
      <JSONLD
        data={breadcrumbs([
          { name: "Inicio", url: "/" },
          { name: "Términos y Condiciones", url: "/terminos" },
        ])}
      />
      <PublicLayout
        heroContent={
          <LegalHeroContent
            icon={FileText}
            title="Términos y Condiciones"
            subtitle="Información legal sobre el uso de nuestros servicios de hospedaje"
          />
        }
      >
        <div className="mx-auto w-full max-w-7xl px-4 md:px-8 py-10 md:py-14">
          <div className="flex flex-col lg:flex-row gap-10 lg:gap-16">
            <PageTOC sections={sections} />

            <div className="flex-1 min-w-0 space-y-3 scroll-smooth">
              {sections.map((section) => {
                const Icon = sectionIcons[section.id] || FileText;
                const isLast = section.id === "12";

                return (
                  <section
                    key={section.id}
                    id={section.id}
                    className={`scroll-mt-32 rounded-xl border p-5 sm:p-6 md:p-7 ${
                      isLast
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
                    <div className="ml-11 sm:ml-11">
                      <p className="text-stone-600 leading-relaxed font-sans text-[15px] break-words">
                        {section.content}
                      </p>
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
