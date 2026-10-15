import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { PQRS_STATUS_CONFIG } from "../config/estados";
import { formatDateTimeCompact } from "@/utils/date";
import { useCambiarEstadoPqrs } from "../hooks/usePqrsList";
import type { PQRSResponse, EstadoPqrs } from "../types";
import {
  Loader2,
  CheckCircle2,
  XCircle,
  Eye,
  MessageSquare,
  RotateCcw,
} from "lucide-react";

const TIPO_LABELS: Record<string, string> = {
  QUEJA: "Queja",
  PETICION: "Petición",
  RECLAMO: "Reclamo",
  SUGERENCIA: "Sugerencia",
  FELICITACION: "Felicitación",
  REPORTE_TECNICO: "Reporte Técnico",
};

interface PqrsDetailModalProps {
  pqrs: PQRSResponse | null;
  open: boolean;
  onClose: () => void;
}

type Accion = "revisar" | "responder" | "cerrar" | "rechazar" | "reabrir" | null;

export default function PqrsDetailModal({ pqrs, open, onClose }: PqrsDetailModalProps) {
  const [accion, setAccion] = useState<Accion>(null);
  const [respuestaTexto, setRespuestaTexto] = useState("");

  const cambiarEstado = useCambiarEstadoPqrs();

  if (!pqrs) return null;

  const getNextActions = (): { accion: Accion; label: string; icon: React.ReactNode; variant?: "hotel" | "outline" | "destructive" }[] => {
    switch (pqrs.estado) {
      case "ABIERTA":
        return [
          { accion: "revisar", label: "Revisar", icon: <Eye className="size-4" /> },
          { accion: "rechazar", label: "Rechazar", icon: <XCircle className="size-4" />, variant: "destructive" },
        ];
      case "EN_REVISION":
        return [
          { accion: "responder", label: "Responder", icon: <MessageSquare className="size-4" />, variant: "hotel" },
          { accion: "rechazar", label: "Rechazar", icon: <XCircle className="size-4" />, variant: "destructive" },
        ];
      case "RESPONDIDA":
        return [
          { accion: "cerrar", label: "Cerrar", icon: <CheckCircle2 className="size-4" /> },
        ];
      case "CERRADA":
      case "RECHAZADA":
        return [
          { accion: "reabrir", label: "Reabrir", icon: <RotateCcw className="size-4" /> },
        ];
      default:
        return [];
    }
  };

  const executeAction = async () => {
    if (!accion) return;
    try {
      const estadoMap: Record<string, EstadoPqrs> = {
        revisar: "EN_REVISION",
        responder: "RESPONDIDA",
        cerrar: "CERRADA",
        rechazar: "RECHAZADA",
        reabrir: "ABIERTA",
      };
      await cambiarEstado.mutateAsync({
        id: pqrs.id,
        estado: estadoMap[accion],
        respuesta: (accion === "responder" || accion === "rechazar") ? respuestaTexto : undefined,
      });
      setAccion(null);
      setRespuestaTexto("");
      onClose();
    } catch { /* handled by mutation */ }
  };

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) { setAccion(null); setRespuestaTexto(""); onClose(); } }}>
      <DialogContent className="sm:max-w-2xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="font-heading flex items-center gap-2">
            <span className="font-mono text-sm">{pqrs.codigo}</span>
            <StatusBadge status={pqrs.estado} configMap={PQRS_STATUS_CONFIG} />
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3 text-sm min-w-0">
            <div>
              <p className="text-xs text-muted-foreground">Tipo</p>
              <p className="font-medium">{TIPO_LABELS[pqrs.tipo] ?? pqrs.tipo}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Prioridad</p>
              <p className="font-medium">{pqrs.prioridad}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Contacto</p>
              <p className="font-medium">{pqrs.nombreContacto || pqrs.usuarioNombre || "—"}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Email</p>
              <p className="font-medium">{pqrs.emailContacto || pqrs.usuarioEmail || "—"}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Teléfono</p>
              <p className="font-medium">{pqrs.telefonoContacto || "—"}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Creado</p>
              <p className="font-medium">{formatDateTimeCompact(pqrs.fechaCreacion)}</p>
            </div>
            {pqrs.codigoReserva && (
              <div className="col-span-2">
                <p className="text-xs text-muted-foreground">Reserva asociada</p>
                <p className="font-mono text-sm">{pqrs.codigoReserva}</p>
              </div>
            )}
          </div>

          <div>
            <Label className="text-xs text-muted-foreground">Asunto</Label>
            <p className="text-sm font-medium">{pqrs.asunto}</p>
          </div>

          <div>
            <Label className="text-xs text-muted-foreground">Descripción</Label>
            <p className="text-sm whitespace-pre-wrap break-words rounded-lg bg-muted p-3">{pqrs.descripcion}</p>
          </div>

          {pqrs.respuesta && (
            <div>
              <Label className="text-xs text-muted-foreground">Respuesta</Label>
              <p className="text-sm whitespace-pre-wrap break-words rounded-lg bg-emerald-50 dark:bg-emerald-950 p-3 border border-emerald-200 dark:border-emerald-800">
                {pqrs.respuesta}
              </p>
            </div>
          )}

          {accion === "responder" && (
            <div className="space-y-2">
              <Label>Respuesta *</Label>
              <Textarea
                placeholder="Escribe la respuesta para el solicitante..."
                value={respuestaTexto}
                onChange={(e) => setRespuestaTexto(e.target.value)}
                className="min-h-24"
              />
            </div>
          )}

          {accion === "rechazar" && (
            <div className="space-y-2">
              <Label>Motivo del rechazo</Label>
              <Textarea
                placeholder="Indica el motivo del rechazo..."
                value={respuestaTexto}
                onChange={(e) => setRespuestaTexto(e.target.value)}
                className="min-h-20"
              />
            </div>
          )}

          <div className="flex flex-wrap gap-2 pt-2 border-t">
            {!accion && getNextActions().map((a) => (
              <Button
                key={a.accion}
                variant={a.variant || "outline"}
                size="sm"
                onClick={() => setAccion(a.accion)}
              >
                {a.icon}
                {a.label}
              </Button>
            ))}
            {accion && (
              <>
                <Button
                  variant="hotel"
                  size="sm"
                  onClick={executeAction}
                  disabled={cambiarEstado.isPending || ((accion === "responder" || accion === "rechazar") && !respuestaTexto.trim())}
                >
                  {cambiarEstado.isPending ? (
                    <Loader2 className="size-4 animate-spin" />
                  ) : (
                    <CheckCircle2 className="size-4" />
                  )}
                  Confirmar
                </Button>
                <Button variant="outline" size="sm" onClick={() => { setAccion(null); setRespuestaTexto(""); }}>
                  Cancelar
                </Button>
              </>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
