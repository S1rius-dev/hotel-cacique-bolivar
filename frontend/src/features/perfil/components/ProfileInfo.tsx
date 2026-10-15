import { Mail, Phone, IdCard, MapPin, BadgeCheck } from "lucide-react";
import ProfileAvatar from "./ProfileAvatar";

interface ClienteData {
  nombreCompleto?: string;
  apellidoCompleto?: string;
  telefono?: string;
  tipoIdentificacion?: string;
  cedula?: string;
  municipioExpedicion?: string;
}

interface Props {
  user: {
    email: string;
    username?: string;
    profilePicture?: string;
  };
  cliente?: ClienteData;
}

export default function ProfileInfo({ user, cliente }: Props) {
  const fullName = cliente
    ? [cliente.nombreCompleto, cliente.apellidoCompleto].filter(Boolean).join(" ")
    : user.username;

  const infoItems = cliente
    ? [
        { icon: Phone, label: "Teléfono", value: cliente.telefono },
        {
          icon: IdCard,
          label: "Identificación",
          value:
            cliente.tipoIdentificacion && cliente.cedula
              ? `${cliente.tipoIdentificacion}: ${cliente.cedula}`
              : undefined,
        },
        { icon: MapPin, label: "Municipio", value: cliente.municipioExpedicion },
      ].filter((i) => i.value)
    : [];

  return (
    <div className="flex flex-col items-center gap-4 py-4">
      <ProfileAvatar
        profilePicture={user.profilePicture}
        name={fullName}
        size="lg"
      />

      <div className="text-center">
        <h2 className="font-heading text-xl font-semibold text-stone-800">
          {fullName ?? "Usuario"}
        </h2>
        <div className="mt-1.5 flex items-center justify-center gap-1.5 text-sm text-stone-400">
          <Mail className="size-3.5" />
          {user.email}
        </div>
      </div>

      {cliente && (
        <div className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 border border-emerald-200">
          <BadgeCheck className="size-3.5" />
          Cliente verificado
        </div>
      )}

      {infoItems.length > 0 && (
        <div className="mt-2 w-full divide-y divide-stone-100 rounded-xl border border-stone-200 bg-white">
          {infoItems.map((item) => (
            <div
              key={item.label}
              className="flex items-center gap-3 px-4 py-3 text-sm"
            >
              <div className="flex size-8 items-center justify-center rounded-lg bg-rose-50 shrink-0">
                <item.icon className="size-4 text-rose-500" />
              </div>
              <div className="min-w-0">
                <p className="text-xs text-stone-400">{item.label}</p>
                <p className="font-medium text-stone-700 truncate">{item.value}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
