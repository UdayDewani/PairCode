import { api } from './api'
type EventHandler = (event: { type: string; data: any }) => void
type BinaryHandler = (update: Uint8Array) => void
export class RoomSocket {
  private socket?: WebSocket; private retry?: ReturnType<typeof setTimeout>; private stopped = false
  constructor(private roomId: string, private onEvent: EventHandler, private onBinary?: BinaryHandler) {}
  connect() { const token = api.token(); if (!token) return; const base = process.env.NEXT_PUBLIC_WS_URL ?? 'ws://localhost:8080/ws'; this.socket = new WebSocket(`${base}?token=${encodeURIComponent(token)}`); this.socket.binaryType = 'arraybuffer'; this.socket.onopen = () => this.send('join', { roomId: this.roomId }); this.socket.onmessage = async e => { if (typeof e.data === 'string') this.onEvent(JSON.parse(e.data)); else this.onBinary?.(new Uint8Array(e.data instanceof Blob ? await e.data.arrayBuffer() : e.data)) }; this.socket.onclose = () => { if (!this.stopped) this.retry = setTimeout(() => this.connect(), 1000) } }
  send(type: string, payload: any = {}) { if (this.socket?.readyState === WebSocket.OPEN) this.socket.send(JSON.stringify({ type, ...payload })) }
  sendYjsUpdate(update: Uint8Array) { if (this.socket?.readyState === WebSocket.OPEN) this.socket.send(update) }
  close() { this.stopped = true; if (this.retry) clearTimeout(this.retry); this.socket?.close() }
}
