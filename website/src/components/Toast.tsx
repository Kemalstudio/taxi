import { useCallback, useRef, useState } from "react";

export interface ToastItem {
  id: number;
  text: string;
}

/** In-app toasts, plus a browser Notification when the tab is backgrounded. */
export function useToasts() {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const counter = useRef(0);

  const notify = useCallback((text: string) => {
    const id = counter.current++;
    setToasts((list) => [...list, { id, text }]);
    setTimeout(() => setToasts((list) => list.filter((item) => item.id !== id)), 4000);

    if (document.hidden && "Notification" in window && Notification.permission === "granted") {
      try {
        new Notification("Taksi Go", { body: text });
      } catch {
        /* best-effort */
      }
    }
  }, []);

  return { toasts, notify };
}

export function requestNotificationPermission() {
  if ("Notification" in window && Notification.permission === "default") {
    Notification.requestPermission().catch(() => {});
  }
}

export function ToastStack({ toasts }: { toasts: ToastItem[] }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-stack">
      {toasts.map((item) => (
        <div className="toast" key={item.id}>
          {item.text}
        </div>
      ))}
    </div>
  );
}
