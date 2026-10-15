import type { LucideIcon } from "lucide-react";

interface Props {
  label: string;
  value: string | number;
  sublabel?: string;
  icon?: LucideIcon;
  tone?: "default" | "success" | "warning" | "danger" | "accent";
}

const toneClasses: Record<string, string> = {
  default: "text-muted-foreground border-border",
  success: "text-[#7bd88f] border-[#7bd88f]/30",
  warning: "text-[#e8b13a] border-[#e8b13a]/30",
  danger: "text-[#e0616a] border-[#e0616a]/30",
  accent: "text-[#c81e3a] border-[#c81e3a]/30",
};

export default function MetricCard({ label, value, sublabel, icon: Icon, tone = "default" }: Props) {
  return (
    <div className="rounded-xl border bg-card border-border p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <span className="text-xs font-medium tracking-widest uppercase text-muted-foreground">{label}</span>
        {Icon && (
          <span className={`size-8 rounded-lg bg-muted border flex items-center justify-center ${toneClasses[tone]}`}>
            <Icon className="size-4" />
          </span>
        )}
      </div>
      <p className="text-2xl font-bold tracking-tight text-foreground">{value}</p>
      {sublabel && <p className="text-xs text-muted-foreground">{sublabel}</p>}
    </div>
  );
}
