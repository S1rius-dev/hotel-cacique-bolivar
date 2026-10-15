import { useEffect, useState } from "react";
import { useClientes } from "@/features/clientes/hooks/useClientes";
import { useClientesStats } from "@/features/clientes/hooks/useClientesStats";
import { clientColumns } from "@/features/clientes/components/clientColumns";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { RotateCcw, Users } from "lucide-react";
import { Button } from "@/components/ui/button";

const DEFAULT_SIZE = 15;

function FiltroInput({
  placeholder,
  value,
  onChange,
}: {
  placeholder: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <Input
      placeholder={placeholder}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className="h-8 text-xs w-full sm:w-[145px] bg-white dark:bg-zinc-950 border-zinc-200 dark:border-zinc-700 focus-visible:ring-1 focus-visible:ring-rose-400"
    />
  );
}

const ClientesPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const size = parseInt(searchParams.get("size") ?? String(DEFAULT_SIZE), 10);
  const nombreParam = searchParams.get("nombre") ?? "";
  const cedulaParam = searchParams.get("cedula") ?? "";
  const telefonoParam = searchParams.get("telefono") ?? "";
  const activoParam = searchParams.get("activo") ?? "";

  const [nombre, setNombre] = useState(nombreParam);
  const [cedula, setCedula] = useState(cedulaParam);
  const [telefono, setTelefono] = useState(telefonoParam);
  const [activo, setActivo] = useState(activoParam);

  const debouncedNombre = useDebounce(nombre, 300);
  const debouncedCedula = useDebounce(cedula, 300);
  const debouncedTelefono = useDebounce(telefono, 300);

  useEffect(() => {
    const urlNombre = searchParams.get("nombre") ?? "";
    const urlCedula = searchParams.get("cedula") ?? "";
    const urlTelefono = searchParams.get("telefono") ?? "";
    const urlActivo = searchParams.get("activo") ?? "";

    if (
      debouncedNombre === urlNombre &&
      debouncedCedula === urlCedula &&
      debouncedTelefono === urlTelefono &&
      activo === urlActivo
    )
      return;

    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (debouncedNombre) next.set("nombre", debouncedNombre);
      else next.delete("nombre");
      if (debouncedCedula) next.set("cedula", debouncedCedula);
      else next.delete("cedula");
      if (debouncedTelefono) next.set("telefono", debouncedTelefono);
      else next.delete("telefono");
      if (activo) next.set("activo", activo);
      else next.delete("activo");
      next.set("page", "0");
      return next;
    });
  }, [
    debouncedNombre,
    debouncedCedula,
    debouncedTelefono,
    activo,
    searchParams,
    setSearchParams,
  ]);

  const { total, activos, inactivos, isLoading: statsLoading } = useClientesStats();

  const { data, isLoading } = useClientes({
    page,
    size,
    nombre: nombreParam || undefined,
    cedula: cedulaParam || undefined,
    telefono: telefonoParam || undefined,
    activo: activoParam ? activoParam === "true" : undefined,
  });

  const clientes = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;
  const currentPage = data?.page ?? 0;

  const hayFiltros = !!(nombre || cedula || telefono || activo);

  const limpiarFiltros = () => {
    setNombre("");
    setCedula("");
    setTelefono("");
    setActivo("");
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.delete("nombre");
      next.delete("cedula");
      next.delete("telefono");
      next.delete("activo");
      next.set("page", "0");
      next.set("size", String(pageSize || 15));
      return next;
    });
  };

  const filterBar = (
    <div className="grid grid-cols-2 sm:flex sm:flex-wrap items-center gap-2">
      <FiltroInput placeholder="Cédula" value={cedula} onChange={setCedula} />
      <FiltroInput
        placeholder="Nombre o apellido"
        value={nombre}
        onChange={setNombre}
      />
      <FiltroInput
        placeholder="Teléfono"
        value={telefono}
        onChange={setTelefono}
      />

      <div className="w-px h-6 bg-border mx-1 max-sm:hidden" />

      <Select
        value={activo}
        onValueChange={(v) => setActivo(v === "all" ? "" : v)}>
        <SelectTrigger className="w-full sm:w-[120px] h-8 text-xs bg-white dark:bg-zinc-950 border-zinc-200 dark:border-zinc-700 focus-visible:ring-1 focus-visible:ring-rose-400">
          <SelectValue placeholder="Estado" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">Todos</SelectItem>
          <SelectItem value="true">Activo</SelectItem>
          <SelectItem value="false">Inactivo</SelectItem>
        </SelectContent>
      </Select>

      {hayFiltros && (
        <Button
          variant="ghost"
          size="sm"
          onClick={limpiarFiltros}
          className="col-span-full sm:col-auto h-8 px-2 text-xs text-muted-foreground hover:text-destructive gap-1">
          <RotateCcw className="w-3 h-3" />
          Limpiar
        </Button>
      )}
    </div>
  );

  return (
    <ManagementTablePage
      title="Clientes"
      titleIcon={<Users className="w-6 h-6 text-rose-600" />}
      subtitle="Gestiona registros, reservas y actividad"
      stats={
        isLoading || statsLoading
          ? undefined
          : [
              { label: "registros", value: total.toLocaleString("es-CO") },
              { label: "activos", value: activos.toLocaleString("es-CO") },
              { label: "inactivos", value: inactivos.toLocaleString("es-CO") },
            ]
      }
      data={clientes}
      columns={clientColumns}
      loading={isLoading}
      createLabel="Nuevo Cliente"
      onCreate={() => navigate("/dashboard/clientes/nuevo")}
      onRowClick={(row) => navigate(`/dashboard/clientes/${row.id}`)}
      currentPage={currentPage + 1}
      totalPages={totalPages}
      totalItems={totalElements}
      pageSize={pageSize}
      onPageChange={(p) =>
        setSearchParams((prev) => {
          const next = new URLSearchParams(prev);
          next.set("page", String(p - 1));
          next.set("size", String(pageSize));
          return next;
        })
      }
      filterBar={filterBar}
    />
  );
};

export default ClientesPage;
