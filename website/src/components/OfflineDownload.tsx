import { useEffect, useState } from "react";
import { Download, Check, Loader } from "lucide-react";
import { useI18n } from "../i18n";
import { downloadCityMap, isCityMapCached } from "../lib/offlineMap";

type State = "idle" | "busy" | "done";

export function OfflineDownload() {
  const { t } = useI18n();
  const [state, setState] = useState<State>("idle");
  const [pct, setPct] = useState(0);

  // Already downloaded on an earlier visit? Show it as done instead of offering it again.
  useEffect(() => {
    isCityMapCached().then((cached) => {
      if (cached) setState("done");
    });
  }, []);

  const start = async () => {
    if (state === "busy") return;
    setState("busy");
    setPct(0);
    await downloadCityMap((done, total) => setPct(Math.round((done / total) * 100)));
    setState("done");
  };

  return (
    <button className={`offline-dl${state === "done" ? " done" : ""}`} onClick={start} disabled={state === "busy"}>
      {state === "idle" && (
        <>
          <Download size={16} /> {t("off.download")}
        </>
      )}
      {state === "busy" && (
        <>
          <Loader size={16} className="spin" /> {t("off.downloading")} {pct}%
        </>
      )}
      {state === "done" && (
        <>
          <Check size={16} /> {t("off.done")}
        </>
      )}
    </button>
  );
}
