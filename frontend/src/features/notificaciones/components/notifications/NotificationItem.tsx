import type { Notificacion } from "../../types";
import {
  NOTIFICACION_TIPO_TITULO,
  NOTIFICACION_TIPO_COLOR,
  NOTIFICACION_ORIGEN,
} from "../../constants";
import { TYPE_ICON, ORIGEN_ICON } from "./notificationTypeIcons";
import { getExtraFields } from "./notificationExtraFields";
import { timeAgo } from "./notificationUtils";

interface NotificationItemProps {
  notification: Notificacion;
  onSelect: (n: Notificacion) => void;
}

export default function NotificationItem({
  notification: n,
  onSelect,
}: NotificationItemProps) {
  const color = NOTIFICACION_TIPO_COLOR[n.tipo];
  const origenConfig = n.origen ? NOTIFICACION_ORIGEN[n.origen] : null;
  const origenIcon = n.origen ? ORIGEN_ICON[n.origen] : null;
  const extraFields = getExtraFields(n.tipo, n.detalles);

  return (
    <button
      type="button"
      onClick={() => onSelect(n)}
      className={`w-full flex items-start gap-3 px-4 py-3 mb-1.5 text-left transition-all duration-150 hover:bg-muted/20 focus-visible:outline-none border-l-[3px] ${
        n.leida
          ? "border-l-transparent"
          : `${color.border} bg-rose-500/8`
      }`}>
      <span
        className={`mt-1 shrink-0 flex size-7 items-center justify-center rounded-lg ${
          n.leida
            ? "bg-muted-foreground/10"
            : color.iconCircle
        }`}>
        {TYPE_ICON[n.tipo]}
      </span>
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-2 min-w-0">
            <span
              className={`text-sm truncate ${
                n.leida
                  ? "text-foreground/70"
                  : "font-semibold text-foreground"
              }`}>
              {NOTIFICACION_TIPO_TITULO[n.tipo]}
            </span>
            {!n.leida && (
              <span
                className={`shrink-0 size-1.5 rounded-full ${color.dot}`}
              />
            )}
          </div>
          {origenConfig && (
            <span className="shrink-0 flex items-center gap-0.5 px-1.5 py-0.5 rounded-full bg-muted/60 text-[10px] font-medium text-muted-foreground/70 leading-none">
              {origenIcon}
              {origenConfig.label}
            </span>
          )}
        </div>
        <p className="text-xs text-muted-foreground truncate mt-1.5 text-left">
          {n.mensaje}
        </p>
        {extraFields.length > 0 && (
          <div className="flex flex-wrap gap-x-3 gap-y-1 mt-2">
            {extraFields.map((f, i) => (
              <span
                key={i}
                className="inline-flex items-center gap-1 text-xs text-muted-foreground/70">
                {f.icon}
                <span>{f.value}</span>
              </span>
            ))}
          </div>
        )}
        <p className="text-[11px] text-muted-foreground/50 mt-1.5 text-left">
          {timeAgo(n.fechaCreacion)}
        </p>
      </div>
    </button>
  );
}
