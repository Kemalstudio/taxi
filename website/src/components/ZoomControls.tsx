import { useState } from "react";
import type maplibregl from "maplibre-gl";
import { BUILDINGS_3D_ZOOM } from "../lib/mapStyle";

/** Buildings are fully extruded just above this zoom, so turning 3D on zooms in to it. */
const MIN_3D_ZOOM = BUILDINGS_3D_ZOOM + 0.6;

export function ZoomControls({ mapRef }: { mapRef: React.MutableRefObject<maplibregl.Map | null> }) {
  const [is3d, setIs3d] = useState(false);

  const toggle3d = () => {
    const map = mapRef.current;
    if (!map) return;
    const next = !is3d;
    setIs3d(next);
    map.easeTo({
      pitch: next ? 58 : 0,
      bearing: next ? -18 : 0,
      // tilting only shows off the 3D buildings once we're zoomed in far enough for them
      zoom: next ? Math.max(map.getZoom(), MIN_3D_ZOOM) : map.getZoom(),
      duration: 700,
    });
  };

  return (
    <div className="zoomctl">
      <button className="dim-btn" onClick={toggle3d} title="2D / 3D">
        {is3d ? "2D" : "3D"}
      </button>
      <button aria-label="Приблизить" onClick={() => mapRef.current?.zoomIn()}>
        +
      </button>
      <button aria-label="Отдалить" onClick={() => mapRef.current?.zoomOut()}>
        −
      </button>
    </div>
  );
}
