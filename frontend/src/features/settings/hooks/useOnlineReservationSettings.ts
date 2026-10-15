import { useCallback, useState } from "react";
import { toast } from "sonner";
import type {
  OnlineReservationRules,
  OnlineReservationSettings,
} from "../types";
import { ONLINE_RESERVATION_DEFAULTS } from "../components/constants";

const STORAGE_KEY = "hc:settings:online-reservations";

type RuleKey = keyof OnlineReservationRules;

function loadSettings(): OnlineReservationSettings {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { webEnabled: true, rules: { ...ONLINE_RESERVATION_DEFAULTS } };
    }
    const parsed = JSON.parse(raw) as Partial<OnlineReservationSettings>;
    return {
      webEnabled: parsed.webEnabled ?? true,
      rules: { ...ONLINE_RESERVATION_DEFAULTS, ...(parsed.rules ?? {}) },
    };
  } catch {
    return { webEnabled: true, rules: { ...ONLINE_RESERVATION_DEFAULTS } };
  }
}

function persist(settings: OnlineReservationSettings) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
  } catch {
    // almacenamiento no disponible: se ignora
  }
}

export function useOnlineReservationSettings() {
  const [settings, setSettings] = useState<OnlineReservationSettings>(loadSettings);
  const [dirty, setDirty] = useState(false);

  const setWebEnabled = useCallback((webEnabled: boolean) => {
    setSettings((prev) => {
      const next = { ...prev, webEnabled };
      persist(next);
      return next;
    });
  }, []);

  const updateRule = useCallback((key: RuleKey, value: number | string | boolean) => {
    setSettings((prev) => ({
      ...prev,
      rules: { ...prev.rules, [key]: value },
    }));
    setDirty(true);
  }, []);

  const saveRules = useCallback(() => {
    setSettings((prev) => {
      persist(prev);
      return prev;
    });
    setDirty(false);
    toast.success("Configuración de reservas online guardada correctamente");
  }, []);

  return { settings, dirty, setWebEnabled, updateRule, saveRules };
}