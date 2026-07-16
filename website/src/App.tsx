import { useEffect, useMemo, useRef, useState } from "react";
import type maplibregl from "maplibre-gl";
import { TopNav } from "./components/TopNav";
import { PromoCards, Footer } from "./components/PromoCards";
import { MapView } from "./components/MapView";
import { ZoomControls } from "./components/ZoomControls";
import { AddressCard, type FieldTarget } from "./components/AddressCard";
import { SavedGrid } from "./components/SavedGrid";
import { OptionsCard, type OptionsState } from "./components/OptionsCard";
import { TariffCard } from "./components/TariffCard";
import { PromoField } from "./components/PromoField";
import { ScheduleCard } from "./components/ScheduleCard";
import { OrderModal, type OrderSummary } from "./components/OrderModal";
import { LoginModal, type Session } from "./components/LoginModal";
import { OfflineBadge } from "./components/OfflineBadge";
import { OfflineDownload } from "./components/OfflineDownload";
import { ActiveRideCard } from "./components/ActiveRideCard";
import { ChatPanel } from "./components/ChatPanel";
import { RatingModal } from "./components/RatingModal";
import { SosSheet } from "./components/SosSheet";
import { ToastStack, useToasts, requestNotificationPermission } from "./components/Toast";
import { DEFAULT_FROM } from "./data/places";
import { priceFor, routeThrough } from "./lib/routing";
import {
  createRide,
  getRide,
  cancelRide,
  token as apiToken,
  userId as apiUserId,
  role as apiRole,
  ApiError,
  NetworkError,
  type PromoPreview,
} from "./lib/api";
import { RideSocket, type RideChatMsg } from "./lib/rideSocket";
import { ProfileModal } from "./components/ProfileModal";
import { useI18n } from "./i18n";
import type { AddressField, GeoPoint, RideDetails, RideMode, RideStatus, RideTariff, RouteResult, StopRow } from "./types";

const emptyField = (): AddressField => ({ text: "", point: null });
const ENDED_STATUSES: RideStatus[] = ["COMPLETED", "CANCELLED", "NO_DRIVERS_FOUND"];

export default function App() {
  const { t } = useI18n();
  const mapRef = useRef<maplibregl.Map | null>(null);

  const [session, setSession] = useState<Session | null>(null);
  const [authOpen, setAuthOpen] = useState(false);

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

  const [tariff, setTariff] = useState<RideTariff>("ECONOMY");
  const [promo, setPromo] = useState<PromoPreview | null>(null);

  const [route, setRoute] = useState<RouteResult | null>(null);
  const [summary, setSummary] = useState<OrderSummary | null>(null);
  const [hintKey, setHintKey] = useState("hint.default");
  const [me, setMe] = useState<GeoPoint | null>(null);
  const [driver, setDriver] = useState<GeoPoint | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const socketRef = useRef<RideSocket | null>(null);

  const [activeRide, setActiveRide] = useState<RideDetails | null>(null);
  const [chatOpen, setChatOpen] = useState(false);
  const chatOpenRef = useRef(false);
  const [chatIncoming, setChatIncoming] = useState<RideChatMsg[]>([]);
  const [unreadChat, setUnreadChat] = useState(false);
  const [sosOpen, setSosOpen] = useState(false);
  const [ratingRideId, setRatingRideId] = useState<string | null>(null);
  const { toasts, notify } = useToasts();

  useEffect(() => {
    chatOpenRef.current = chatOpen;
  }, [chatOpen]);

  const startTracking = (rideId: string, fare: number | null) => {
    socketRef.current?.disconnect();
    setDriver(null);
    setChatIncoming([]);
    setUnreadChat(false);
    setChatOpen(false);
    setActiveRide({
      id: rideId,
      passengerId: session?.userId ?? "",
      driverId: null,
      status: "SEARCHING",
      tariff,
      fare,
      promoCode: promo?.code ?? null,
      discountApplied: promo?.discountAmount ?? null,
      driver: null,
    });
    requestNotificationPermission();

    const sock = new RideSocket(rideId, {
      onLocation: (m) => setDriver({ label: "driver", lat: m.lat, lng: m.lng }),
      onStatus: (m) => {
        const status = m.status as RideStatus;
        notify(t(`ride.status.${status}`) ?? status);
        setActiveRide((prev) => (prev ? { ...prev, status, driverId: m.driverId } : prev));

        if (status === "ACCEPTED" || status === "DRIVER_ARRIVED") {
          getRide(rideId)
            .then(setActiveRide)
            .catch(() => {});
        }
        if (status === "COMPLETED") {
          setRatingRideId(rideId);
        }
        if (ENDED_STATUSES.includes(status)) {
          setActiveRide(null);
          setDriver(null);
          socketRef.current?.disconnect();
        }
      },
      onMessage: (m) => {
        setChatIncoming((list) => [...list, m]);
        if (!chatOpenRef.current) {
          setUnreadChat(true);
          notify(m.body);
        }
      },
    });
    sock.connect();
    socketRef.current = sock;
  };

  useEffect(() => () => socketRef.current?.disconnect(), []);

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

  // ---- GPS "my location" ----
  const useMyLocation = () => {
    if (!navigator.geolocation) {
      setHintKey("hint.gpsErr");
      return;
    }
    setHintKey("hint.gps");
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const point: GeoPoint = {
          label: t("addr.gps"),
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        };
        setMe(point);
        setFrom({ text: point.label, point });
        mapRef.current?.flyTo({ center: [point.lng, point.lat], zoom: 15, essential: true });
        setHintKey("hint.gpsOk");
      },
      (err) => {
        setHintKey(err.code === err.PERMISSION_DENIED ? "hint.gpsDenied" : "hint.gpsErr");
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 0 },
    );
  };

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
    setHintKey("hint.routing");
    routeThrough(orderedPoints).then((r) => {
      if (cancelled) return;
      setRoute(r);
      setHintKey("hint.cash");
    });
    return () => {
      cancelled = true;
    };
  }, [orderedPoints, from.point, to.point]);

  // ---- derived fare / labels ----
  const price = route ? priceFor(route.km, tariff) : null;
  const finalPrice = promo ? promo.finalFare : price;
  const canOrder = Boolean(from.point && to.point && route);
  const fareText = finalPrice != null ? `${finalPrice} TMT` : "— TMT";
  const distText = route ? `<b>${route.km.toFixed(1)}</b> ${t("sch.km")}` : "—";
  const timeText = route ? `≈ <b>${Math.round(route.min)}</b> ${t("sch.min")}` : "";
  const orderLabel =
    finalPrice != null ? `${mode === "later" ? t("sch.book") : t("sch.order")} · ${finalPrice} TMT` : "";

  // A previously applied promo was priced against the old fare — drop it once the fare changes.
  useEffect(() => {
    setPromo(null);
  }, [price, tariff]);

  const buildRows = (): [string, string][] => {
    const rows: [string, string][] = [[t("m.route"), `${from.point!.label} → ${to.point!.label}`]];
    const stopLabels = stops.filter((s) => s.field.point).map((s) => s.field.point!.label);
    if (stopLabels.length) rows.push([t("m.stops"), stopLabels.join(", ")]);
    rows.push([t("m.price"), `${finalPrice} TMT · ${t("m.cash")}`]);
    if (promo) rows.push([t("promo.discount"), `−${promo.discountAmount} TMT (${promo.code})`]);
    if (options.comment.trim()) rows.push([t("m.comment"), options.comment.trim()]);
    if (options.otherOpen && options.otherName.trim()) {
      rows.push([t("m.passenger"), `${options.otherName.trim()} · +993 ${options.otherPhone.trim() || "—"}`]);
    }
    return rows;
  };

  const submit = async () => {
    if (!canOrder || price == null || !from.point || !to.point) return;
    const rows = buildRows();

    const booking = mode === "later";
    if (booking) rows.push([t("m.atTime"), `${date} ${time}`]);
    const okTitle = booking ? t("m.bookTitle") : t("m.orderTitle");
    const okSub = booking ? t("m.bookSub") : t("m.orderSub");

    // Signed in against the real backend → create the ride (now, or SCHEDULED for later);
    // it shows up in the shared admin dashboard.
    if (session?.online && apiToken.get()) {
      const scheduledAt = booking ? new Date(`${date}T${time}`).toISOString() : undefined;
      try {
        const ride = await createRide(from.point, to.point, scheduledAt, tariff, price, promo?.code);
        setSummary({ title: okTitle, subtitle: okSub, rows: [...rows, [t("m.rideNo"), ride.id.slice(0, 8)]] });
        if (!booking) startTracking(ride.id, finalPrice); // live-track the driver once dispatched
      } catch (e) {
        if (e instanceof ApiError) setSummary({ title: t("m.failTitle"), subtitle: e.message, rows });
        else if (e instanceof NetworkError) setSummary({ title: okTitle, subtitle: t("m.demo"), rows });
        else setSummary({ title: t("m.failTitle"), subtitle: String(e), rows });
      }
      return;
    }

    // Not signed in (or demo session): local confirmation.
    setSummary({ title: okTitle, subtitle: okSub, rows });
  };

  const cancelActiveRide = async () => {
    if (!activeRide) return;
    try {
      await cancelRide(activeRide.id);
    } catch {
      /* best-effort — the status socket will reconcile if it actually stayed active */
    }
    setActiveRide(null);
    setDriver(null);
    socketRef.current?.disconnect();
  };

  return (
    <>
      <MapView
        mapRef={mapRef}
        from={from.point}
        to={to.point}
        stops={stops.map((s) => s.field.point).filter(Boolean) as GeoPoint[]}
        route={route}
        me={me}
        driver={driver}
      />

      <TopNav
        session={session}
        onSignIn={() => setAuthOpen(true)}
        onProfile={() => setProfileOpen(true)}
      />
      <PromoCards />
      <ZoomControls mapRef={mapRef} />

      {activeRide ? (
        <ActiveRideCard
          ride={activeRide}
          onChat={() => {
            setChatOpen(true);
            setUnreadChat(false);
          }}
          onSos={() => setSosOpen(true)}
          onCancel={cancelActiveRide}
          unreadChat={unreadChat}
        />
      ) : (
        <div className="panel">
          <AddressCard
            from={from}
            to={to}
            stops={stops}
            onText={setText}
            onPick={pick}
            onAddStop={addStop}
            onRemoveStop={removeStop}
            onGps={useMyLocation}
          />
          <SavedGrid onPick={pickSaved} />
          <OptionsCard value={options} onChange={(patch) => setOptions((o) => ({ ...o, ...patch }))} />
          <TariffCard value={tariff} onChange={setTariff} km={route?.km ?? null} />
          <PromoField fare={price} applied={promo} onApplied={setPromo} />
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
            hint={t(hintKey)}
            onOrder={submit}
          />
        </div>
      )}

      <Footer />
      <OfflineDownload />
      <OfflineBadge />
      <ToastStack toasts={toasts} />

      {summary && <OrderModal summary={summary} onClose={() => setSummary(null)} />}
      {authOpen && (
        <LoginModal
          onClose={() => setAuthOpen(false)}
          onAuth={(s) => {
            setSession(s);
            setAuthOpen(false);
          }}
        />
      )}
      {profileOpen && session && (
        <ProfileModal
          session={session}
          onClose={() => setProfileOpen(false)}
          onLogout={() => {
            apiToken.clear();
            apiUserId.clear();
            apiRole.clear();
            setSession(null);
            setProfileOpen(false);
            socketRef.current?.disconnect();
            setDriver(null);
            setActiveRide(null);
          }}
        />
      )}
      {chatOpen && activeRide && (
        <ChatPanel
          rideId={activeRide.id}
          myUserId={session?.userId ?? null}
          incoming={chatIncoming}
          onClose={() => setChatOpen(false)}
        />
      )}
      {sosOpen && activeRide && (
        <SosSheet
          rideId={activeRide.id}
          point={driver ?? from.point}
          from={from.point}
          to={to.point}
          onClose={() => setSosOpen(false)}
        />
      )}
      {ratingRideId && <RatingModal rideId={ratingRideId} onClose={() => setRatingRideId(null)} />}
    </>
  );
}
