import { useEffect, useMemo, useRef, useState } from "react";
import type L from "leaflet";
import { TopNav } from "./components/TopNav";
import { PromoCards, Footer } from "./components/PromoCards";
import { MapView } from "./components/MapView";
import { ZoomControls } from "./components/ZoomControls";
import { AddressCard, type FieldTarget } from "./components/AddressCard";
import { SavedGrid } from "./components/SavedGrid";
import { OptionsCard, type OptionsState } from "./components/OptionsCard";
import { ScheduleCard } from "./components/ScheduleCard";
import { OrderModal, type OrderSummary } from "./components/OrderModal";
import { DEFAULT_FROM } from "./data/places";
import { priceFor, routeThrough } from "./lib/routing";
import type { AddressField, GeoPoint, RideMode, RouteResult, StopRow } from "./types";

const emptyField = (): AddressField => ({ text: "", point: null });

export default function App() {
  const mapRef = useRef<L.Map | null>(null);

  const [from, setFrom] = useState<AddressField>({
    text: DEFAULT_FROM.label,
    point: { label: DEFAULT_FROM.label, lat: DEFAULT_FROM.lat, lng: DEFAULT_FROM.lng },
  });
  const [to, setTo] = useState<AddressField>(emptyField());
  const [stops, setStops] = useState<StopRow[]>([]);
  const stopId = useRef(0);

  const [mode, setMode] = useState<RideMode>("now");
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [time, setTime] = useState("08:00");

  const [options, setOptions] = useState<OptionsState>({
    comment: "",
    otherOpen: false,
    otherName: "",
    otherPhone: "",
    textOnly: false,
    wheelchair: false,
  });

  const [route, setRoute] = useState<RouteResult | null>(null);
  const [summary, setSummary] = useState<OrderSummary | null>(null);
  const [hint, setHint] = useState("Введите адрес «Откуда» и «Куда» — маршрут построится по картам");

  // ---- address editing ----
  const setText = (target: FieldTarget, text: string) => {
    if (target === "from") setFrom((f) => ({ ...f, text }));
    else if (target === "to") setTo((f) => ({ ...f, text }));
    else {
      const id = Number(target.split("-")[1]);
      setStops((rows) => rows.map((r) => (r.id === id ? { ...r, field: { ...r.field, text } } : r)));
    }
  };
  const pick = (target: FieldTarget, point: GeoPoint) => {
    const field = { text: point.label, point };
    if (target === "from") setFrom(field);
    else if (target === "to") setTo(field);
    else {
      const id = Number(target.split("-")[1]);
      setStops((rows) => rows.map((r) => (r.id === id ? { ...r, field } : r)));
    }
  };
  const addStop = () => setStops((rows) => [...rows, { id: stopId.current++, field: emptyField() }]);
  const removeStop = (id: number) => setStops((rows) => rows.filter((r) => r.id !== id));
  const pickSaved = (point: GeoPoint) => setTo({ text: point.label, point });

  // ---- routing ----
  const orderedPoints = useMemo<GeoPoint[]>(() => {
    const pts: GeoPoint[] = [];
    if (from.point) pts.push(from.point);
    stops.forEach((s) => s.field.point && pts.push(s.field.point));
    if (to.point) pts.push(to.point);
    return pts;
  }, [from.point, to.point, stops]);

  useEffect(() => {
    if (!from.point || !to.point) {
      setRoute(null);
      return;
    }
    let cancelled = false;
    setHint("Строим маршрут по картам…");
    routeThrough(orderedPoints).then((r) => {
      if (cancelled) return;
      setRoute(r);
      setHint("Оплата наличными водителю (манат)");
    });
    return () => {
      cancelled = true;
    };
  }, [orderedPoints, from.point, to.point]);

  // ---- derived fare / labels ----
  const price = route ? priceFor(route.km) : null;
  const canOrder = Boolean(from.point && to.point && route);
  const fareText = price != null ? `${price} TMT` : "— TMT";
  const distText = route ? `<b>${route.km.toFixed(1)}</b> км` : "—";
  const timeText = route ? `≈ <b>${Math.round(route.min)}</b> мин` : "";
  const orderLabel =
    price != null
      ? mode === "later"
        ? `Забронировать · ${price} TMT`
        : `Заказать · ${price} TMT`
      : "";

  const submit = () => {
    if (!canOrder || price == null) return;
    const rows: [string, string][] = [
      ["Маршрут", `${from.point!.label} → ${to.point!.label}`],
    ];
    const stopLabels = stops.filter((s) => s.field.point).map((s) => s.field.point!.label);
    if (stopLabels.length) rows.push(["Остановки", stopLabels.join(", ")]);
    rows.push(["Стоимость", `${price} TMT · наличными`]);
    if (options.comment.trim()) rows.push(["Комментарий", options.comment.trim()]);
    if (options.otherOpen && options.otherName.trim()) {
      rows.push(["Пассажир", `${options.otherName.trim()} · +993 ${options.otherPhone.trim() || "—"}`]);
    }
    if (mode === "later") {
      rows.push(["Ко времени", `${date} ${time}`]);
      setSummary({ title: "Поездка забронирована", subtitle: "Мы подадим машину к указанному времени.", rows });
    } else {
      setSummary({ title: "Заказ принят", subtitle: "Ищем ближайшего водителя…", rows });
    }
  };

  return (
    <>
      <MapView
        mapRef={mapRef}
        from={from.point}
        to={to.point}
        stops={stops.map((s) => s.field.point).filter(Boolean) as GeoPoint[]}
        route={route}
      />

      <TopNav />
      <PromoCards />
      <ZoomControls mapRef={mapRef} />

      <div className="panel">
        <AddressCard
          from={from}
          to={to}
          stops={stops}
          onText={setText}
          onPick={pick}
          onAddStop={addStop}
          onRemoveStop={removeStop}
        />
        <SavedGrid onPick={pickSaved} />
        <OptionsCard value={options} onChange={(patch) => setOptions((o) => ({ ...o, ...patch }))} />
        <ScheduleCard
          mode={mode}
          onMode={setMode}
          date={date}
          time={time}
          onDate={setDate}
          onTime={setTime}
          fareText={fareText}
          distText={distText}
          timeText={timeText}
          canOrder={canOrder}
          orderLabel={orderLabel}
          hint={hint}
          onOrder={submit}
        />
      </div>

      <Footer />

      {summary && <OrderModal summary={summary} onClose={() => setSummary(null)} />}
    </>
  );
}
