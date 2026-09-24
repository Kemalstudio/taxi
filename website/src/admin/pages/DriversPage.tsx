import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Car, Search, Star, Trophy } from "lucide-react";
import { useDrivers, useRides } from "../hooks/queries";
import { Card, EmptyState, Spinner, StatusBadge } from "../components/ui/primitives";
import { Badge } from "@/admin/components/ui/badge";
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "@/admin/components/ui/table";
import { Avatar } from "@/admin/components/ui/avatar";
import { ExportCsvButton } from "@/admin/components/ui/export-csv-button";
import type { CsvColumn } from "../lib/csv";
import type { AdminDriver, DriverStatus } from "../types";

type DriverRow = AdminDriver & { completedRides: number; totalEarned: number; todayRides: number; todayEarned: number };
const CSV_COLUMNS: CsvColumn<DriverRow>[] = [
  { label: "Name", value: d => d.fullName }, { label: "Phone", value: d => d.phone ?? "" },
  { label: "Status", value: d => d.status }, { label: "Plate", value: d => d.plateNumber ?? "" },
  { label: "Rating", value: d => d.rating }, { label: "Completed rides", value: d => d.completedRides },
  { label: "Total earned TMT", value: d => d.totalEarned }, { label: "Today rides", value: d => d.todayRides },
  { label: "Today earned TMT", value: d => d.todayEarned },
];
const VERIFY_TONE: Record<string, "neutral" | "success" | "danger"> = { PENDING: "neutral", APPROVED: "success", REJECTED: "danger" };

export function DriversPage() {
  const { data: drivers = [], isLoading } = useDrivers();
  const { data: rides = [] } = useRides("ALL", 5000);
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<"ALL" | DriverStatus>("ALL");
  const [sort, setSort] = useState<"RATING" | "RIDES" | "EARNINGS" | "NAME">("RATING");
  const rows = useMemo<DriverRow[]>(() => {
    const sums = new Map<string, { completedRides: number; totalEarned: number; todayRides: number; todayEarned: number }>();
    const today = new Date().toDateString();
    rides.forEach(ride => {
      if (!ride.driverId || ride.status !== "COMPLETED") return;
      const s = sums.get(ride.driverId) ?? { completedRides: 0, totalEarned: 0, todayRides: 0, todayEarned: 0 };
      s.completedRides++; s.totalEarned += ride.fare ?? 0;
      if (new Date(ride.completedAt ?? ride.requestedAt).toDateString() === today) { s.todayRides++; s.todayEarned += ride.fare ?? 0; }
      sums.set(ride.driverId, s);
    });
    const q = query.trim().toLowerCase();
    return drivers.map(d => ({ ...d, ...(sums.get(d.userId) ?? { completedRides: 0, totalEarned: 0, todayRides: 0, todayEarned: 0 }) }))
      .filter(d => (status === "ALL" || d.status === status) && (!q || [d.fullName,d.email,d.phone,d.plateNumber,d.vehicleMake,d.vehicleModel].some(v => v?.toLowerCase().includes(q))))
      .sort((a,b) => sort === "RATING" ? Number(b.rating)-Number(a.rating) : sort === "RIDES" ? b.completedRides-a.completedRides : sort === "EARNINGS" ? b.totalEarned-a.totalEarned : a.fullName.localeCompare(b.fullName));
  }, [drivers, rides, query, status, sort]);
  const best = rows[0];
  return <div className="flex flex-col gap-6">
    <div className="flex flex-wrap items-start justify-between gap-4"><div><h1 className="text-2xl font-semibold">Driver performance</h1><p className="text-sm text-mist-500">Live status, rating, completed orders and earnings</p></div><div className="flex gap-2"><Link to="/fleet-map" className="flex items-center gap-2 rounded-xl bg-accent px-4 py-2.5 text-sm font-semibold text-ink-900"><Car size={17}/>Open live map</Link><ExportCsvButton rows={rows} columns={CSV_COLUMNS} filename="driver-performance.csv"/></div></div>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4"><Card><p className="text-xs text-mist-600">Total drivers</p><p className="mt-1 text-2xl font-semibold tnum">{drivers.length}</p></Card><Card><p className="text-xs text-mist-600">Available now</p><p className="mt-1 text-2xl font-semibold tnum text-success">{drivers.filter(d=>d.status==="ONLINE").length}</p></Card><Card><p className="text-xs text-mist-600">Fleet earnings</p><p className="mt-1 text-2xl font-semibold tnum">{rows.reduce((s,d)=>s+d.totalEarned,0)} TMT</p></Card><Card><p className="text-xs text-mist-600">Top rated driver</p><p className="mt-1 truncate text-base font-semibold text-amber"><Trophy className="mr-1 inline" size={16}/>{best?.fullName ?? "—"}</p></Card></div>
    <Card className="flex flex-wrap items-center gap-3"><div className="flex min-w-[240px] flex-1 items-center gap-2 rounded-xl border border-white/10 bg-ink-800 px-3"><Search size={16} className="text-mist-600"/><input className="h-11 w-full bg-transparent text-sm outline-none placeholder:text-mist-600" value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search name, phone, plate or vehicle…"/></div><select value={status} onChange={e=>setStatus(e.target.value as typeof status)} className="field w-auto py-2.5"><option value="ALL">All statuses</option><option>ONLINE</option><option>BUSY</option><option>OFFLINE</option></select><select value={sort} onChange={e=>setSort(e.target.value as typeof sort)} className="field w-auto py-2.5"><option value="RATING">Best rating</option><option value="RIDES">Most rides</option><option value="EARNINGS">Highest earnings</option><option value="NAME">Name A–Z</option></select></Card>
    <Card className="p-0">{isLoading ? <div className="p-6"><Spinner label="Loading drivers…"/></div> : rows.length === 0 ? <EmptyState message="No drivers match these filters."/> : <Table className="min-w-[1100px]"><TableHeader><TableRow><TableHead># / Driver</TableHead><TableHead>Status</TableHead><TableHead>Vehicle</TableHead><TableHead>Rating</TableHead><TableHead>Completed</TableHead><TableHead>Today</TableHead><TableHead>Total earned</TableHead><TableHead>Verification</TableHead><TableHead/></TableRow></TableHeader><TableBody>{rows.map((d,i)=><TableRow key={d.userId}><TableCell><div className="flex items-center gap-3"><span className={`w-6 text-center text-xs font-semibold ${i<3?"text-amber":"text-mist-600"}`}>{i+1}</span><Avatar name={d.fullName}/><div><p className="font-medium text-mist-100">{d.fullName}</p><p className="text-xs text-mist-600">{d.phone ?? d.email}</p></div></div></TableCell><TableCell><div className="flex gap-1"><StatusBadge status={d.status}/>{d.banned&&<Badge tone="danger">banned</Badge>}</div></TableCell><TableCell><p className="text-mist-300">{[d.vehicleMake,d.vehicleModel].filter(Boolean).join(" ")||"—"}</p><p className="font-mono text-xs text-mist-600">{d.plateNumber??"No plate"}</p></TableCell><TableCell><span className="font-semibold text-amber"><Star className="mr-1 inline fill-current" size={13}/>{d.rating}</span></TableCell><TableCell><span className="tnum font-semibold">{d.completedRides}</span> rides</TableCell><TableCell><p className="tnum font-semibold">{d.todayRides} rides</p><p className="text-xs text-success">{d.todayEarned} TMT</p></TableCell><TableCell><span className="tnum font-semibold">{d.totalEarned} TMT</span></TableCell><TableCell><Badge tone={VERIFY_TONE[d.verificationStatus]}>{d.verificationStatus.toLowerCase()}</Badge></TableCell><TableCell><Link to={`/drivers/${d.userId}`} className="text-xs font-semibold text-accent hover:underline">Full profile →</Link></TableCell></TableRow>)}</TableBody></Table>}</Card>
  </div>;
}
