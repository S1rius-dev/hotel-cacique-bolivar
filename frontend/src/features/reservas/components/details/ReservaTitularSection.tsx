import { User, BadgeCheck, FileText, Phone, Fingerprint, IdCard, ExternalLink } from "lucide-react";
import { useNavigate } from "react-router-dom";
import type { TitularInfo } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  titular?: TitularInfo;
}

function ReservaTitularSection({ titular }: Props) {
  const navigate = useNavigate();
  if (!titular) {
    return (
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-emerald-500">
        <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
          <IdCard className="h-5 w-5 text-emerald-600" />
          Titular de la Reserva
        </h3>
        <p className="text-sm text-muted-foreground">Sin información del titular</p>
      </section>
    );
  }

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-emerald-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <IdCard className="h-5 w-5 text-emerald-600" />
        Titular de la Reserva
      </h3>

      <div className="flex items-start gap-4 p-4 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40">
        <User className="w-5 h-5 text-emerald-500 mt-0.5 shrink-0" />

        <div className="min-w-0 flex-1 space-y-3">
          <div>
            <p className="text-sm text-muted-foreground">Nombre completo</p>
            <p className="text-base font-semibold text-zinc-900 dark:text-zinc-100 break-words">
              {titular.nombreCompleto ?? ""} {titular.apellidoCompleto ?? ""}
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-3 min-w-0">
            {titular.tipoIdentificacion && (
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <BadgeCheck className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                  Tipo ID
                </p>
                <p className="text-sm font-medium text-zinc-700 dark:text-zinc-300 break-words">
                  {titular.tipoIdentificacion}
                </p>
              </div>
            )}

            {titular.cedula && (
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <Fingerprint className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                  Cédula
                </p>
                <p className="text-sm font-medium text-zinc-700 dark:text-zinc-300 break-words">
                  {titular.cedula}
                </p>
              </div>
            )}

            {titular.telefono && (
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <Phone className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                  Teléfono
                </p>
                <p className="text-sm font-medium text-zinc-700 dark:text-zinc-300 break-words">
                  {titular.telefono}
                </p>
              </div>
            )}

            {titular.id != null && (
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <FileText className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                  ID Cliente
                </p>
                <button
                  onClick={() => navigate(`/dashboard/clientes/${titular.id}`)}
                  className="text-sm font-medium text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300 flex items-center gap-1 transition-colors"
                  title="Ver detalle del cliente"
                >
                  {titular.id}
                  <ExternalLink className="w-3 h-3" />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

export default ReservaTitularSection;
