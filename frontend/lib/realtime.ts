import { api } from './api'
type EventHandler = (event: { type: string; data: unknown }) => void
type BinaryHandler = (update: Uint8Array) => void
type StatusHandler = (status: 'connected' | 'reconnecting') => void

export class RoomSocket {
  private socket?: WebSocket
  private retry?: ReturnType<typeof setTimeout>
  private stopped = false
  private attempt = 0
  private onStatus?: StatusHandler

  constructor(
    private roomId: string,
    private onEvent: EventHandler,
    private onBinary?: BinaryHandler,
  ) {}

  setOnStatus(handler: StatusHandler) { this.onStatus = handler }

  async connect() {
    if (this.stopped) return
    this.onStatus?.('reconnecting')
    // Fetch a fresh short-lived ticket before every connect attempt
    let ticket: string | null = null
    try { const t = await api.wsTicket(); ticket = t.ticket } catch { /* fall back to token */ }
    const base = process.env.NEXT_PUBLIC_WS_URL ?? 'ws://localhost:8080/ws'
    const url = ticket
      ? `${base}?ticket=${encodeURIComponent(ticket)}`
      : `${base}?token=${encodeURIComponent(api.token() ?? '')}`
    this.socket = new WebSocket(url)
    this.socket.binaryType = 'arraybuffer'
    this.socket.onopen = () => {
      this.attempt = 0
      this.send('join', { roomId: this.roomId })
      this.onStatus?.('connected')
    }
    this.socket.onmessage = async e => {
      if (typeof e.data === 'string') this.onEvent(JSON.parse(e.data))
      else this.onBinary?.(new Uint8Array(e.data instanceof Blob ? await e.data.arrayBuffer() : e.data))
    }
    this.socket.onclose = () => {
      if (this.stopped) return
      this.onStatus?.('reconnecting')
      this.attempt++
      // Exponential backoff with jitter: base 1s, max 30s
      const delay = Math.min(1000 * Math.pow(2, this.attempt - 1), 30_000) + Math.random() * 500
      this.retry = setTimeout(() => this.connect(), delay)
    }
  }

  send(type: string, payload: unknown = {}) {
    if (this.socket?.readyState === WebSocket.OPEN)
      this.socket.send(JSON.stringify({ type, ...payload as object }))
  }

  sendYjsUpdate(update: Uint8Array) {
    if (this.socket?.readyState === WebSocket.OPEN) this.socket.send(update)
  }

  close() { this.stopped = true; if (this.retry) clearTimeout(this.retry); this.socket?.close() }
}
