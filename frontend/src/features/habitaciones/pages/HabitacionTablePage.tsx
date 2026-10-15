import React from "react";

import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";

import { useRoom, useTypeRoom } from "@/features/habitaciones/hooks/useRoom";
import { useHabitacionesStats } from "@/features/habitaciones/hooks/useHabitacionesStats";

import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";

import {
  roomColumns,
  typeRoomColumns,
} from "@/features/habitaciones/components/roomColumns";

import { useNavigate, useSearchParams } from "react-router-dom";
import { DoorOpen } from "lucide-react";

const DEFAULT_SIZE = 15;

const tabTriggerClass = `
  rounded-lg px-4 py-2 text-sm font-medium
  transition-all duration-200

  text-muted-foreground
  hover:text-rose-700
  hover:bg-rose-700/5

  data-[state=active]:bg-rose-700
  data-[state=active]:text-white
  data-[state=active]:shadow-md

  dark:text-zinc-300
  dark:hover:text-white
  dark:hover:bg-rose-700/10
  dark:data-[state=active]:bg-rose-700
  dark:data-[state=active]:text-white
  dark:data-[state=active]:shadow-lg
`;

const HabitacionPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const size = parseInt(searchParams.get("size") ?? String(DEFAULT_SIZE), 10);

  const navigate = useNavigate();

  const [tab, setTab] = React.useState<"habitaciones" | "tipos">(
    "habitaciones",
  );

  const isRooms = tab === "habitaciones";

  const { totalHabitaciones, totalTipos, isLoading: statsLoading } = useHabitacionesStats();

  const { data: roomData, isLoading: loadingRooms } = useRoom(
    isRooms ? page : 0,
    size,
  );
  const { data: typeData, isLoading: loadingTypes } = useTypeRoom(
    isRooms ? 0 : page,
    size,
  );

  const data = isRooms ? roomData : typeData;
  const items = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;
  const currentPage = data?.page ?? 0;

  return (
    <ManagementTablePage
      title={isRooms ? "Gestión de Habitaciones" : "Tipos de habitación"}
      titleIcon={<DoorOpen className="w-6 h-6 text-rose-600" />}
      subtitle="Administra habitaciones y tipos de alojamiento"
      stats={
        (isRooms ? loadingRooms : loadingTypes) || statsLoading
          ? undefined
          : [
              { label: "habitaciones", value: totalHabitaciones.toLocaleString("es-CO") },
              { label: "tipos", value: totalTipos.toLocaleString("es-CO") },
            ]
      }
      data={items}
      columns={isRooms ? roomColumns : typeRoomColumns}
      loading={isRooms ? loadingRooms : loadingTypes}
      createLabel={isRooms ? "Agregar habitación" : "Agregar tipo"}
      onCreate={() =>
        navigate(
          isRooms
            ? "/dashboard/habitaciones/nuevo"
            : "/dashboard/habitaciones/tipos/nuevo",
        )
      }
      onRowClick={(row) =>
        navigate(
          isRooms
            ? `/dashboard/habitaciones/${row.id}`
            : `/dashboard/habitaciones/tipos/${row.id}`,
        )
      }
      toolbar={
        <Tabs
          value={tab}
          onValueChange={(v) => {
            setTab(v === "tipos" ? "tipos" : "habitaciones");
            setSearchParams({ page: "0", size: String(pageSize || size || 0) });
          }}>
          <TabsList
            className="
              flex gap-2 rounded-xl
              border border-zinc-200 dark:border-zinc-800
              bg-white/40 dark:bg-zinc-900/40
              p-1 shadow-sm backdrop-blur
            ">
            <TabsTrigger value="habitaciones" className={tabTriggerClass}>
              Habitaciones
            </TabsTrigger>

            <TabsTrigger value="tipos" className={tabTriggerClass}>
              Tipos
            </TabsTrigger>
          </TabsList>
        </Tabs>
      }
      currentPage={currentPage + 1}
      totalPages={totalPages}
      totalItems={totalElements}
      pageSize={pageSize}
      onPageChange={(p) =>
        setSearchParams({ page: String(p - 1), size: String(pageSize) })
      }
    />
  );
};

export default HabitacionPage;
