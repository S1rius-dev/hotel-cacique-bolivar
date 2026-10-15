import { cn } from "@/lib/utils";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

import RoomShowcase from "./RoomShowcase";
import type { Room } from "./rooms.data";

type RoomTabsProps = {
  /**
   * Las tarjetas llegan ya combinadas con los precios del backend, así que
   * este componente no puede importarlas: la primera iteración llega vacía
   * mientras la consulta está en curso.
   */
  rooms: Room[];
};

export default function RoomTabs({ rooms }: RoomTabsProps) {
  if (rooms.length === 0) return null;

  return (
    <>
      <style>
        {`@keyframes roomTabFadeIn {
          from { opacity: 0; transform: translateY(24px); }
          to   { opacity: 1; transform: translateY(0); }
        }`}
      </style>

      <Tabs defaultValue={rooms[0].id} className="w-full">
        <TabsList
          variant="line"
          className={cn(
            "self-center h-auto p-0 mb-12 md:mb-16",
            "inline-flex justify-center",
            "gap-8 md:gap-14",
          )}>
          {rooms.map((room) => (
            <TabsTrigger
              key={room.id}
              value={room.id}
              className={cn(
                "px-2 py-3",
                "font-sans text-xs md:text-sm font-medium",
                "uppercase tracking-[0.2em]",

                "text-stone-400!",
                "hover:text-stone-700!",
                "data-[state=active]:text-stone-900!",

                "transition-colors duration-300",

                "data-[state=active]:[&::after]:opacity-100",
                "data-[state=active]:[&::after]:scale-x-100",
                "data-[state=active]:[&::after]:bg-rose-500",

                "after:scale-x-0",
                "after:origin-center",
                "after:transition-all",
                "after:duration-500",
              )}>
              {room.tabLabel}
            </TabsTrigger>
          ))}
        </TabsList>

        {rooms.map((room) => (
          <TabsContent
            key={room.id}
            value={room.id}
            className="w-full outline-none"
            style={{ animation: "roomTabFadeIn 600ms ease-out" }}>
            <RoomShowcase room={room} />
          </TabsContent>
        ))}
      </Tabs>
    </>
  );
}
