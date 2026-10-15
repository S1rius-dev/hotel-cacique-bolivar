import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { GeneralSettings } from "./GeneralSettings";
import { OnlineBookings } from "./OnlineBookings";

const TABS = [
  { value: "general", label: "Ajustes generales" },
  { value: "reservas", label: "Reservas online" },
] as const;

export function SettingsTabs() {
  return (
    <div className="flex h-full flex-col">
      <Tabs defaultValue="general" className="flex flex-1 flex-col">
        <div className="border-b px-4 sm:px-6">
          <TabsList variant="line" className="h-10 gap-0">
            {TABS.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className="rounded-none px-4 text-sm data-[state=active]:text-rose-800 dark:data-[state=active]:text-rose-400"
              >
                {tab.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </div>

        <div className="flex-1 overflow-auto p-4 sm:p-6">
          <TabsContent value="general" className="mt-0">
            <GeneralSettings />
          </TabsContent>
          <TabsContent value="reservas" className="mt-0">
            <OnlineBookings />
          </TabsContent>
        </div>
      </Tabs>
    </div>
  );
}
