import type L from "leaflet";

export function ZoomControls({ mapRef }: { mapRef: React.MutableRefObject<L.Map | null> }) {
  return (
    <div className="zoomctl">
      <button aria-label="Приблизить" onClick={() => mapRef.current?.zoomIn()}>
        +
      </button>
      <button aria-label="Отдалить" onClick={() => mapRef.current?.zoomOut()}>
        −
      </button>
    </div>
  );
}
