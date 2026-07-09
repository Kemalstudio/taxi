import { useState } from "react";
import type maplibregl from "maplibre-gl";

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
      duration: 650,
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
