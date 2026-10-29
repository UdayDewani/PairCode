export type User = { id: string; name: string; email: string; preferredLanguage: 'PYTHON' | 'JAVA' | 'CPP' }
export type ExampleItem = { input: string; output: string; explanation?: string }
export type StarterCode = { PYTHON?: string; JAVA?: string; CPP?: string }
export type Problem = { id: string; number: string; title: string; difficulty: 'EASY' | 'MEDIUM' | 'HARD'; topic: string; acceptanceRate: number; description?: string; examples?: ExampleItem[]; constraints?: string; starterCode?: StarterCode }
export type Room = { id: string; roomId: string; name: string; status: 'ACTIVE' | 'ENDED'; language: 'PYTHON' | 'JAVA' | 'CPP'; candidateCanEdit: boolean; editorLocked: boolean; documentState?: string | null; problem?: Problem; participants: Participant[] }
export type Participant = { id: string; name: string; email: string; role: 'INTERVIEWER' | 'CANDIDATE'; online: boolean }
export type ChatMessage = { id: string; author: string; authorId: string; content: string; type: string; createdAt: string }

const base = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api/v1'
let accessToken = typeof window === 'undefined' ? null : localStorage.getItem('paircode.access')
let refreshToken = typeof window === 'undefined' ? null : localStorage.getItem('paircode.refresh')
function saveTokens(access: string | null, refresh: string | null) { accessToken = access; refreshToken = refresh; if (typeof window !== 'undefined') { access ? localStorage.setItem('paircode.access', access) : localStorage.removeItem('paircode.access'); refresh ? localStorage.setItem('paircode.refresh', refresh) : localStorage.removeItem('paircode.refresh') } }

// Single-flight refresh: one shared Promise for concurrent 401s
let refreshPromise: Promise<boolean> | null = null
async function doRefresh(): Promise<boolean> {
  if (!refreshToken) return false
  try {
    const refreshed = await fetch(`${base}/auth/refresh`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken }) })
    if (refreshed.ok) { const auth = await refreshed.json(); saveTokens(auth.accessToken, auth.refreshToken); return true }
    saveTokens(null, null); return false
  } catch { saveTokens(null, null); return false }
}

class ApiError extends Error { constructor(readonly status: number, message: string) { super(message) } }

async function request<T>(path: string, options: RequestInit = {}, retried = false): Promise<T> {
  const headers = new Headers(options.headers); headers.set('Content-Type', 'application/json'); if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  const response = await fetch(`${base}${path}`, { ...options, headers })
  if (response.status === 401 && refreshToken && !retried) {
    if (!refreshPromise) { refreshPromise = doRefresh().finally(() => { refreshPromise = null }) }
    const ok = await refreshPromise
    if (ok) return request(path, options, true)
    throw new ApiError(401, 'Session expired')
  }
  if (!response.ok) { const error = await response.json().catch(() => null); throw new ApiError(response.status, error?.message ?? `Request failed (${response.status})`) }
  return response.status === 204 ? undefined as T : response.json()
}

export function parseProblem(raw: Record<string, unknown>): Problem {
  let examples: ExampleItem[] = []
  let starterCode: StarterCode = {}
  try { examples = typeof raw.examples === 'string' ? JSON.parse(raw.examples) : (Array.isArray(raw.examples) ? raw.examples as ExampleItem[] : []) } catch { examples = [] }
  try { starterCode = typeof raw.starterCode === 'string' ? JSON.parse(raw.starterCode) : (raw.starterCode as StarterCode ?? {}) } catch { starterCode = {} }
  return { ...raw as unknown as Problem, examples, starterCode }
}

export function parseRoom(room: Room): Room {
  if (!room) return room
  return {
    ...room,
    problem: room.problem ? parseProblem(room.problem as unknown as Record<string, unknown>) : undefined
  }
}

export const api = {
  token: () => accessToken,
  isError401: (e: unknown): boolean => e instanceof ApiError && e.status === 401,
  async login(email: string, password: string) { const auth = await request<{ accessToken: string; refreshToken: string; user: User }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }); saveTokens(auth.accessToken, auth.refreshToken); return auth },
  async register(name: string, email: string, password: string) { const auth = await request<{ accessToken: string; refreshToken: string; user: User }>('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password }) }); saveTokens(auth.accessToken, auth.refreshToken); return auth },
  logout: async () => { try { if (refreshToken) await request('/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }) } finally { saveTokens(null, null) } },
  profile: () => request<User>('/profile'), updateProfile: (data: Partial<User>) => request<User>('/profile', { method: 'PATCH', body: JSON.stringify(data) }),
  problems: (search = '') => request<{ items: Problem[] }>(`/problems?size=50&search=${encodeURIComponent(search)}`).then(r => ({ ...r, items: r.items.map(p => parseProblem(p as unknown as Record<string, unknown>)) })),
  createRoom: (data: { name: string; problemId?: string; language: string; candidateCanEdit: boolean }) => request<Room>('/rooms', { method: 'POST', body: JSON.stringify(data) }).then(parseRoom),
  room: (code: string) => request<Room>(`/rooms/${code}`).then(parseRoom), joinRoom: (code: string) => request<Room>(`/rooms/${code}/join`, { method: 'POST' }).then(parseRoom),
  permissions: (code: string, data: { candidateCanEdit?: boolean; editorLocked?: boolean }) => request<Room>(`/rooms/${code}/permissions`, { method: 'PATCH', body: JSON.stringify(data) }).then(parseRoom),
  endRoom: (code: string, sourceCode: string) => request<void>(`/rooms/${code}/end`, { method: 'POST', body: JSON.stringify({ sourceCode }) }),
  messages: (code: string) => request<ChatMessage[]>(`/rooms/${code}/messages`), sendMessage: (code: string, content: string) => request<ChatMessage>(`/rooms/${code}/messages`, { method: 'POST', body: JSON.stringify({ content }) }),
  run: (code: string, data: { sourceCode: string; language: string; stdin?: string }) => request(`/rooms/${code}/run`, { method: 'POST', body: JSON.stringify(data) }),
  submit: (code: string, data: { sourceCode: string; language: string; stdin?: string }) => request(`/rooms/${code}/submit`, { method: 'POST', body: JSON.stringify(data) }),
  sessions: () => request<Array<Record<string, unknown>>>('/sessions'), dashboard: () => request<Record<string, unknown>>('/dashboard'),
  wsTicket: () => request<{ ticket: string; expiresAt: string }>('/ws-ticket', { method: 'POST' }),
}
