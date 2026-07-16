import { useEffect, useRef, useState } from "react";
import { X, Send } from "lucide-react";
import { useI18n } from "../i18n";
import { getMessages, sendMessage } from "../lib/api";
import type { RideChatMsg } from "../lib/rideSocket";
import type { ChatMessage } from "../types";

interface Props {
  rideId: string;
  myUserId: string | null;
  incoming: RideChatMsg[];
  onClose: () => void;
}

export function ChatPanel({ rideId, myUserId, incoming, onClose }: Props) {
  const { t } = useI18n();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    getMessages(rideId)
      .then(setMessages)
      .catch(() => {});
  }, [rideId]);

  useEffect(() => {
    if (!incoming.length) return;
    setMessages((list) => {
      const known = new Set(list.map((m) => `${m.senderId}:${m.createdAt}:${m.body}`));
      const fresh = incoming
        .filter((m) => !known.has(`${m.senderId}:${m.createdAt}:${m.body}`))
        .map((m) => ({
          id: `${m.senderId}-${m.createdAt}`,
          rideId: m.rideId,
          senderId: m.senderId,
          senderRole: m.senderRole,
          body: m.body,
          createdAt: m.createdAt,
        }));
      return fresh.length ? [...list, ...fresh] : list;
    });
  }, [incoming]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const submit = async () => {
    const body = text.trim();
    if (!body) return;
    setText("");
    setBusy(true);
    try {
      const sent = await sendMessage(rideId, body);
      setMessages((list) => [...list, sent]);
    } catch {
      /* best-effort — the message may still arrive via the socket */
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal chat-modal">
        <div className="chat-head">
          <span>{t("chat.title")}</span>
          <button className="chat-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        <div className="chat-body">
          {messages.length === 0 && <div className="chat-empty">{t("chat.empty")}</div>}
          {messages.map((m) => (
            <div key={m.id} className={`chat-msg${m.senderId === myUserId ? " mine" : ""}`}>
              {m.body}
            </div>
          ))}
          <div ref={bottomRef} />
        </div>
        <div className="chat-input">
          <input
            placeholder={t("chat.placeholder")}
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && submit()}
          />
          <button onClick={submit} disabled={busy || !text.trim()}>
            <Send size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}
