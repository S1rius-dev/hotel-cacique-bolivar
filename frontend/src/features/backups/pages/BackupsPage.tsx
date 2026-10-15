import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  DatabaseBackup,
  Download,
  Trash2,
  ShieldAlert,
  DatabaseZap,
  ShieldCheck,
  FolderOpen,
} from "lucide-react";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { DataTableActions, type ActionItem } from "@/components/ui/table/data-table/DataTableActions";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { backupColumns } from "../components/backupColumns";
import { BackupFilters } from "../components/BackupFilters";
import CloneBackupDialog from "../components/CloneBackupDialog";
import { asEstado, asTipo } from "../utils/filter-enums";
import { useBackups } from "../hooks/useBackups";
import { useBackupDashboard } from "../hooks/useBackupDashboard";
import { useCreateBackup, formatearBytes } from "../hooks/useCreateBackup";
import { useDeleteBackup } from "../hooks/useDeleteBackup";
import { useDownloadBackup } from "../hooks/useDownloadBackup";
import { useVerifyBackup } from "../hooks/useVerifyBackup";
import { useCloneBackup } from "../hooks/useCloneBackup";
import { esDescargable } from "../config/estados";
import { BACKUP_PAGE_SIZE, type BackupFilters as Filters } from "../types";
import type { BackupResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateTimeFull } from "@/utils/date";

export default function BackupsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const [tipoFiltro, setTipoFiltro] = useState<string>("all");
  const [estadoFiltro, setEstadoFiltro] = useState<string>("all");
  const [aEliminar, setAEliminar] = useState<BackupResponse | null>(null);
  const [aClonar, setAClonar] = useState<BackupResponse | null>(null);

  const filters: Filters = {
    page,
    size: BACKUP_PAGE_SIZE,
    tipo: asTipo(tipoFiltro),
    estado: asEstado(estadoFiltro),
  };

  const { data, isLoading } = useBackups(filters);
  const { data: dashboard } = useBackupDashboard();
  const crear = useCreateBackup();
  const eliminar = useDeleteBackup();
  const { descargar } = useDownloadBackup();
  const verificar = useVerifyBackup();
  const clonar = useCloneBackup();

  const lista = data?.content ?? [];

  const irAPagina = (nueva: number, sobrescribirFiltros = false) => {
    setSearchParams({
      page: String(nueva),
      ...(sobrescribirFiltros
        ? {
            ...(tipoFiltro !== "all" ? { tipo: tipoFiltro } : {}),
            ...(estadoFiltro !== "all" ? { estado: estadoFiltro } : {}),
          }
        : {}),
    });
  };

  const acciones = (row: BackupResponse): ActionItem<BackupResponse>[] => {
    const items: ActionItem<BackupResponse>[] = [];

    // Un FALLIDO puede dejar un archivo parcial en disco que no restaura, asi que la descarga
    // no se ofrece en ese estado.
    if (esDescargable(row.estado) && row.id != null) {
      items.push({
        label: "Descargar .sql.gz",
        icon: Download,
        onClick: (r) => {
          if (r.id == null) return;
          descargar(r.id, r.archivoNombre ?? `backup-${r.id}.sql.gz`);
        },
      });
    }

    if (row.id != null) {
      // Verificar y clonar solo tienen sentido con un archivo completo en disco:
      // un FALLIDO deja un .sql.gz parcial cuyo checksum no cuadra.
      if (esDescargable(row.estado)) {
        items.push({
          label: "Verificar integridad",
          icon: ShieldCheck,
          onClick: (r) => {
            if (r.id == null) return;
            verificar.mutate(r.id);
          },
        });

        items.push({
          label: "Restaurar en base nueva",
          icon: DatabaseZap,
          onClick: (r) => setAClonar(r),
        });
      }

      items.push({
        label: "Eliminar",
        icon: Trash2,
        variant: "destructive",
        onClick: (r) => setAEliminar(r),
      });
    }

    return items;
  };

  const stats = dashboard
    ? [
        {
          value: dashboard.ultimoExitoso
            ? formatDateTimeFull(dashboard.ultimoExitoso)
            : "Nunca",
          label: "Última copia exitosa",
        },
        { value: dashboard.totalCopias ?? 0, label: "Copias" },
        { value: dashboard.totalAutomaticos ?? 0, label: "Automáticas" },
        { value: dashboard.totalManuales ?? 0, label: "Manuales" },
        { value: formatearBytes(dashboard.tamanoTotalBytes), label: "Espacio usado" },
        {
          value: dashboard.totalFallidas ?? 0,
          label: "Fallidas",
        },
      ]
    : undefined;

  const subtitulo = dashboard
    ? dashboard.ejecucionEnCurso
      ? "Hay una copia en ejecución. El listado no se actualiza solo: recarga al terminar."
      : dashboard.proximaEjecucion
        ? `Próxima copia automática: ${formatDateTimeFull(dashboard.proximaEjecucion)}.`
        : "Copias automáticas no programadas (BACKUP_SCHEDULE_ENABLED=false)."
    : "Copia, verificación y restauración de la base de datos.";

  return (
    <>
      <ManagementTablePage
        title="Copias de seguridad"
        titleIcon={<DatabaseBackup className="text-rose-800 dark:text-rose-400" />}
        subtitle={subtitulo}
        stats={stats}
        data={lista}
        columns={backupColumns}
        loading={isLoading}
        createLabel={crear.isPending ? "Creando copia…" : "Crear copia ahora"}
        createPending={crear.isPending}
        onCreate={() => crear.mutate()}
        renderActions={(row) => (
          <DataTableActions row={row} actions={acciones(row)} />
        )}
        tableTitle="Historial"
        tableSubtitle="copias de la base de datos"
        emptyMessage="Todavía no hay copias de seguridad"
        filterBar={
          <BackupFilters
            tipo={tipoFiltro}
            estado={estadoFiltro}
            onTipoChange={(v) => {
              setTipoFiltro(v);
              irAPagina(0, true);
            }}
            onEstadoChange={(v) => {
              setEstadoFiltro(v);
              irAPagina(0, true);
            }}
          />
        }
        currentPage={page + 1}
        totalPages={data?.totalPages ?? 0}
        totalItems={data?.totalElements ?? 0}
        pageSize={data?.size ?? 0}
        onPageChange={(p) => irAPagina(p - 1)}
      />

      {dashboard?.rutaAlmacenamiento && (
        <div className="mx-auto mb-4 w-full max-w-7xl px-4">
          <div className="flex items-start gap-3 rounded-xl border border-stone-200 bg-stone-50 p-4 dark:border-stone-700 dark:bg-stone-900">
            <FolderOpen className="mt-0.5 size-4 shrink-0 text-stone-500" />
            <div className="min-w-0 text-xs leading-relaxed">
              <p className="font-medium text-stone-700 dark:text-stone-200">
                Los archivos .sql.gz se guardan en
              </p>
              <p className="mt-0.5 break-all font-mono text-stone-600 dark:text-stone-400">
                {dashboard.rutaAlmacenamiento}
              </p>
              <p className="mt-1 text-stone-500 dark:text-stone-400">
                En desarrollo, esa carpeta no se versiona en git. Copia los archivos a un
                disco o servicio externo: una copia en el mismo servidor no protege de
                una pérdida de disco.
              </p>
            </div>
          </div>
        </div>
      )}

      <ConfirmDeleteDialog
        open={aEliminar != null}
        onClose={() => setAEliminar(null)}
        onConfirm={() => {
          if (aEliminar?.id == null) return;
          eliminar.mutate(aEliminar.id, {
            onSuccess: () => setAEliminar(null),
          });
        }}
        isPending={eliminar.isPending}
        title="Eliminar copia de seguridad"
        description={
          aEliminar?.estado === "FALLIDO"
            ? `Se eliminará el registro de "${aEliminar.archivoNombre}". El archivo no es restaurable, pero se borra igualmente. Esta acción no se puede deshacer.`
            : `Se eliminará "${aEliminar?.archivoNombre}" de forma permanente. Esta copia contiene datos personales de huéspedes y saldos de caja, y no se puede recuperar. Esta acción no se puede deshacer.`
        }
        confirmLabel="Eliminar definitivamente"
      />

      <CloneBackupDialog
        open={aClonar != null}
        onOpenChange={(v) => {
          if (!v && !clonar.isPending) setAClonar(null);
        }}
        archivoNombre={aClonar?.archivoNombre}
        baseEnUso={dashboard?.baseDeDatosEnUso}
        onConfirm={(baseDestino) => {
          if (aClonar?.id == null) return;
          clonar.mutate(
            { id: aClonar.id, baseDestino },
            { onSuccess: () => setAClonar(null) },
          );
        }}
        isPending={clonar.isPending}
      />

      {dashboard?.ejecucionEnCurso && (
        <div className="fixed bottom-4 right-4 z-40 flex items-center gap-2 rounded-xl border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-800 shadow-sm dark:border-sky-800 dark:bg-sky-950 dark:text-sky-300">
          <ShieldAlert className="size-4 shrink-0" />
          Copia en ejecución
        </div>
      )}
    </>
  );
}
