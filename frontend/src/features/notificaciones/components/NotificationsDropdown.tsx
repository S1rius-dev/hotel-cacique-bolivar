import type { Notificacion } from "../types";
import NotificationItem from "./notifications/NotificationItem";
import NotificationDropdownHeader from "./notifications/NotificationDropdownHeader";
import NotificationEmptyState from "./notifications/NotificationEmptyState";

interface NotificationsDropdownProps {
  notifications: Notificacion[];
  loading?: boolean;
  onSelect: (n: Notificacion) => void;
  onMarkAllRead: () => void;
  onViewAll: () => void;
}

export default function NotificationsDropdown({
  notifications,
  loading,
  onSelect,
  onMarkAllRead,
  onViewAll,
}: NotificationsDropdownProps) {
  const unreadCount = notifications.filter((n) => !n.leida).length;

  return (
    <div className="w-[90vw] sm:w-130">
      <NotificationDropdownHeader
        unreadCount={unreadCount}
        onMarkAllRead={onMarkAllRead}
      />

      <div className="max-h-[60vh] overflow-y-auto overflow-x-hidden">
        {loading ? (
          <div className="flex items-center justify-center py-10">
            <div className="size-5 rounded-full border-2 border-foreground/20 border-t-foreground animate-spin" />
          </div>
        ) : notifications.length === 0 ? (
          <NotificationEmptyState />
        ) : (
          notifications.map((n) => (
            <NotificationItem
              key={n.id}
              notification={n}
              onSelect={onSelect}
            />
          ))
        )}
      </div>

      <button
        type="button"
        onClick={onViewAll}
        className="w-full flex items-center justify-center gap-2 py-3 text-xs font-medium text-foreground/70 hover:text-foreground border-t border-border transition">
        Ver todas las notificaciones
      </button>
    </div>
  );
}
