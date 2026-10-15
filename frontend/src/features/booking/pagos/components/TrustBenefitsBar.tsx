import { Lock, Mail, BadgeDollarSign, Headset } from "lucide-react";

const items = [
  {
    icon: Lock,
    title: "Pago 100% seguro",
    desc: "Encriptación SSL",
  },
  {
    icon: Mail,
    title: "Confirmación inmediata",
    desc: "Recibe tu comprobante",
  },
  {
    icon: BadgeDollarSign,
    title: "Sin cargos ocultos",
    desc: "Precio final garantizado",
  },
  {
    icon: Headset,
    title: "Soporte 24/7",
    desc: "Estamos para ayudarte",
  },
];

export default function TrustBenefitsBar() {
  return (
    <div className="rounded-2xl border border-stone-200 bg-white shadow-sm overflow-hidden">
      <div className="grid grid-cols-2 lg:grid-cols-4 divide-x divide-stone-100 divide-y lg:divide-y-0">
        {items.map((item) => (
          <div key={item.title} className="flex items-center gap-3 px-4 sm:px-6 py-4 sm:py-5">
            <div className="size-9 rounded-xl bg-rose-50 border border-rose-100 flex items-center justify-center shrink-0">
              <item.icon className="size-4 text-rose-600" />
            </div>
            <div className="min-w-0">
              <p className="text-xs sm:text-sm font-semibold text-stone-800 leading-tight">{item.title}</p>
              <p className="text-[11px] text-stone-400 leading-tight mt-0.5 truncate">{item.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
