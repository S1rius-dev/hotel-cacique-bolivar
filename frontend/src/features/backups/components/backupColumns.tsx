import { StatusBadge } from "@/components/ui/StatusBadge";
import type { Column } from "@/components/ui/table/data-table/types";
import { formatDateTimeFull } from "@/utils/date";
import type { BackupResponse } from "@/api/generated/openAPIDefinition.schemas";
import { BACKUP_ESTADO_CONFIG, BACKUP_TIPO_LABEL } from "../config/estados";
import { formatearBytes } from "../hooks/useCreateBackup";

/**
 * El checksum se muestra truncado con el titulo completo: sirve para comparar de un vistazo
 * si dos copias son distintas, sin ocupar media columna con 64 caracteres hexadecimales.
 */
const ChecksumCell = ({ valor }: { valor?: string }) => {
  if (!valor) return <span className="text-muted-foreground">—</span>;
  return (
    <span title={valor} className="font-mono text-xs text-muted-foreground">
      {valor.slice(0, 12)}…
    </span>
  );
};

const DuracionCell = ({ ms }: { ms?: number }) => {
  if (ms == null) return <span className="text-muted-foreground">—</span>;
  if (ms < 1000) return <span className="tabular-nums">{ms} ms</span>;

  const segundos = ms / 1000;
  if (segundos < 60) return <span className="tabular-nums">{segundos.toFixed(1)} s</span>;

  const min = Math.floor(segundos / 60);
  const resto = Math.round(segundos % 60);
  return <span className="tabular-nums">{min} min {resto} s</span>;
};

export const backupColumns: Column<BackupResponse>[] = [
  {
    key: "fecha",
    header: "Fecha",
    render: (row) => <span className="whitespace-nowrap">{formatDateTimeFull(row.fecha)}</span>,
  },
  {
    key: "tipo",
    header: "Origen",
    render: (row) => (
      <span className="whitespace-nowrap">
        {BACKUP_TIPO_LABEL[row.tipo ?? ""] ?? "—"}
      </span>
    ),
  },
  {
    key: "estado",
    header: "Estado",
    render: (row) => <StatusBadge status={row.estado} configMap={BACKUP_ESTADO_CONFIG} />,
  },
  {
    key: "archivoNombre",
    header: "Archivo",
    render: (row) => (
      <span className="font-mono text-xs" title={row.archivoNombre}>
        {row.archivoNombre ?? "—"}
      </span>
    ),
  },
  {
    key: "tamanoBytes",
    header: "Tamaño",
    render: (row) => <span className="tabular-nums whitespace-nowrap">{formatearBytes(row.tamanoBytes)}</span>,
  },
  {
    key: "duracionMs",
    header: "Duración",
    render: (row) => <DuracionCell ms={row.duracionMs} />,
  },
  {
    key: "usuario",
    header: "Origen (usuario)",
    render: (row) => <span className="text-muted-foreground">{row.usuario ?? "Sistema"}</span>,
  },
  {
    key: "checksumSha256",
    header: "SHA-256",
    render: (row) => <ChecksumCell valor={row.checksumSha256} />,
  },
];
