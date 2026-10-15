import { Bell, CheckCheck } from "lucide-react";

interface NotificationDropdownHeaderProps {
  unreadCount: number;
  onMarkAllRead: () => void;
}

export default function NotificationDropdownHeader({
  unreadCount,
  onMarkAllRead,
}: NotificationDropdownHeaderProps) {
  return (
    <div className="flex items-center justify-between px-4 py-3 border-b border-border">
      <div className="flex items-center gap-2">
        <Bell className="size-4 text-foreground" />
        <span className="text-sm font-semibold text-foreground">
          Notificaciones
        </span>
        {unreadCount > 0 && (
          <span className="flex items-center justify-center h-5 min-w-5 px-1.5 rounded-full bg-foreground/20 text-[11px] font-bold text-foreground">
            {unreadCount}
          </span>
        )}
      </div>
      {unreadCount > 0 && (
        <button
          type="button"
          onClick={onMarkAllRead}
          className="flex items-center gap-1 text-xs text-foreground/60 hover:text-foreground transition">
          <CheckCheck className="size-3.5" />
          Leer todo
        </button>
      )}
    </div>
  );
}
