import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/app/auth/AuthContext";
import { getApiUrl } from "@/api/api-config";

const SSE_URL = `${getApiUrl()}/api/sse/subscribe`;

const ALLOWED_ROLES = ["ADMIN", "RECEPCIONISTA"];

const SSE_EVENT_MAP: Record<string, string[][]> = {
  RESERVA_CREADA: [["reservas"], ["habitaciones-disponibles"], ["dashboard"], ["notificaciones"]],
  RESERVA_ACTUALIZADA: [["reservas"], ["reserva"], ["dashboard"]],
  RESERVA_CANCELADA: [["reservas"], ["reserva"], ["dashboard"], ["notificaciones"]],
  RESERVA_VENCIDA: [["reservas"], ["dashboard"]],
  RESERVA_EXTENDIDA: [["reservas"], ["reserva"], ["dashboard"], ["notificaciones"]],
  CHECK_IN: [["estado-diario"], ["reserva"], ["reservas"], ["dashboard"], ["notificaciones"]],
  CHECK_OUT: [["estado-diario"], ["reserva"], ["reservas"], ["dashboard"], ["notificaciones"]],
  PAGO_REGISTRADO: [["pagos"], ["saldo"], ["reserva"], ["reservas"], ["estado-diario"], ["dashboard"], ["notificaciones"]],
  PAGO_REEMBOLSADO: [["pagos"], ["saldo"], ["reserva"], ["reservas"], ["dashboard"], ["notificaciones"]],
  TURNO_ABIERTO: [["turno-activo"], ["dashboard"]],
  TURNO_CERRADO: [["turno-activo"], ["turno-historial"], ["dashboard"]],
  GASTO_REGISTRADO: [["turno-activo"], ["dashboard"]],
  ALERTA_CREADA: [["alertas"]],
  HABITACION_ESTADO_CAMBIADO: [["habitaciones"], ["habitacion"], ["estado-diario"], ["dashboard"]],
  RESERVA_DETALLE_AGREGADO: [["reserva"], ["reservas"], ["estado-diario"], ["dashboard"]],
  RESERVA_DETALLE_ELIMINADO: [["reserva"], ["reservas"], ["estado-diario"], ["dashboard"]],
  AJUSTE_AGREGADO: [["reserva"], ["reservas"], ["dashboard"]],
  NO_SHOW_AUTO: [["reservas"], ["dashboard"], ["notificaciones"]],
  CHECK_OUT_AUTO: [["estado-diario"], ["reserva"], ["reservas"], ["dashboard"], ["notificaciones"]],
  CLIENTE_CREADO: [["clientes"], ["cliente"]],
};

let sharedSource: EventSource | null = null;
let listenersAttached = false;
let refCount = 0;
let registeredQueryClient: ReturnType<typeof useQueryClient> | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let reconnectDelay = 0;
const MAX_DELAY = 30_000;
const IS_DEV = import.meta.env.DEV;

function logSSE(message: string, ...args: unknown[]) {
  if (IS_DEV) {
    console.info(`[SSE] ${message}`, ...args);
  }
}

function attachListeners(qc: ReturnType<typeof useQueryClient>) {
  if (!sharedSource || listenersAttached) return;

  registeredQueryClient = qc;
  listenersAttached = true;

  const handleEvent =
    (eventType: string, keys: string[][]) =>
    (event: MessageEvent) => {
      logSSE(`← ${eventType}`, event.data);
      if (!registeredQueryClient) {
        console.warn(`[SSE] No queryClient registrado — evento ${eventType} ignorado`);
        return;
      }
      try {
        keys.forEach((key) => {
          const matches = registeredQueryClient!.getQueryCache().findAll({ queryKey: key });
          logSSE(`  → ${key[0]}: ${matches.length} query(s) encontrada(s)`);
          registeredQueryClient!.invalidateQueries({
            queryKey: key,
            refetchType: "all",
          });
        });
        logSSE(`  ✓ ${eventType}: invalidación completada`);
      } catch (err) {
        console.warn(`[SSE] Error al invalidar queries para ${eventType}:`, err);
      }
    };

  Object.entries(SSE_EVENT_MAP).forEach(([eventType, keys]) => {
    sharedSource!.addEventListener(eventType, handleEvent(eventType, keys));
  });

  sharedSource.addEventListener("CONNECTED", () => {
    logSSE("Conectado al servicio de eventos");
    reconnectDelay = 0;
  });

  sharedSource.addEventListener("open", () => {
    logSSE("Conexión abierta");
  });

  sharedSource.addEventListener("ping", () => {
    logSSE("Ping recibido");
  });

  sharedSource.addEventListener("error", (event) => {
    const readyState = sharedSource?.readyState;
    if (readyState === EventSource.CLOSED) {
      console.warn("[SSE] Conexión cerrada permanentemente — reconectando...");
    } else if (readyState === EventSource.CONNECTING) {
      logSSE("Reconectando...");
    } else {
      console.warn("[SSE] Error de conexión", event);
    }
    scheduleReconnect();
  });
}

function scheduleReconnect() {
  if (reconnectTimer) return;

  const delay = reconnectDelay === 0 ? 1000 : Math.min(reconnectDelay * 2, MAX_DELAY);
  reconnectDelay = delay;

  logSSE(`Reconectando en ${delay / 1000}s...`);

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    if (refCount > 0 && (!sharedSource || sharedSource.readyState === EventSource.CLOSED)) {
      logSSE("Intento de reconexión");
      openConnection(registeredQueryClient!);
    }
  }, delay);
}

function openConnection(qc: ReturnType<typeof useQueryClient>) {
  if (sharedSource && sharedSource.readyState !== EventSource.CLOSED) return;

  if (sharedSource) {
    const oldState = sharedSource.readyState;
    logSSE(`Cerrando conexión anterior (readyState: ${oldState})`);
    sharedSource.close();
  }

  listenersAttached = false;
  logSSE("Abriendo conexión a", SSE_URL);
  sharedSource = new EventSource(SSE_URL, { withCredentials: true });
  attachListeners(qc);
}

function closeConnection() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  reconnectDelay = 0;

  if (sharedSource) {
    logSSE("Cerrando conexión");
    sharedSource.close();
    sharedSource = null;
    listenersAttached = false;
    registeredQueryClient = null;
  }
}

export const useSSE = () => {
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;

    return () => {
      mountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    const isAllowed = user?.roles?.some((r) => ALLOWED_ROLES.includes(r));
    if (!isAllowed) return;

    refCount++;
    logSSE(`refCount: ${refCount}`);

    openConnection(queryClient);

    return () => {
      refCount--;
      logSSE(`refCount: ${refCount}`);

      if (refCount === 0 && mountedRef.current) {
        closeConnection();
      }
    };
  }, [queryClient, user]);
};
