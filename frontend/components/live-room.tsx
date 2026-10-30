'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Copy, Lock, Play, Send, ShieldCheck, Users } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { api, parseRoom, type ChatMessage, type ExampleItem, type Room, type Submission, type User } from '@/lib/api'
import { RoomSocket } from '@/lib/realtime'

type Props = { room: Room | null; leave: () => void }
type RunResult = Submission

const statusClass: Record<string, string> = {
  SUCCESS: 'success',
  WRONG_ANSWER: 'wrong',
  TIME_LIMIT_EXCEEDED: 'timeout',
  COMPILE_ERROR: 'danger',
  RUNTIME_ERROR: 'danger',
  QUEUED: 'pending',
  RUNNING: 'pending',
}
const statusLabel = (status?: string) => status ? status.replaceAll('_', ' ').toLowerCase().replace(/^./, c => c.toUpperCase()) : 'Unknown'
const EMPTY_OUTPUT_MESSAGE = 'Program finished with exit code 0 and printed nothing. Did you print your answer?'

const encode = (value: string) => btoa(String.fromCharCode(...new TextEncoder().encode(value)))
const decode = (value: string) => new TextDecoder().decode(Uint8Array.from(atob(value), c => c.charCodeAt(0)))
const languageLabel: Record<Room['language'], string> = { PYTHON: 'Python', JAVA: 'Java', CPP: 'C++' }
const fileExtension: Record<Room['language'], string> = { PYTHON: 'py', JAVA: 'java', CPP: 'cpp' }
const editorMode: Record<Room['language'], string> = { PYTHON: 'python', JAVA: 'java', CPP: 'cpp' }

function starterCodeFor(room: Room, language: Room['language']) {
  return room.problem?.starterCode?.[language] ?? ''
}

export function LiveRoom({ room: initial, leave }: Props) {
  const [room, setRoom] = useState<Room | null>(() => initial ? parseRoom(initial) : null)
  const [me, setMe] = useState<User | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [draft, setDraft] = useState('')
  const [source, setSource] = useState('')
  const [status, setStatus] = useState<'connected' | 'reconnecting'>('reconnecting')
  const [run, setRun] = useState<RunResult | null>(null)
  const [running, setRunning] = useState(false)
  const [stdin, setStdin] = useState('')
  const [outputTab, setOutputTab] = useState<'output' | 'tests'>('output')
  const [error, setError] = useState('')
  const [languageNotice, setLanguageNotice] = useState('')
  const [changingLanguage, setChangingLanguage] = useState(false)
  const socket = useRef<RoomSocket | null>(null)
  const pollDelay = useRef<ReturnType<typeof setTimeout> | null>(null)
  const pollTimer = useRef<ReturnType<typeof setInterval> | null>(null)
  const pollAttempts = useRef(0)
  const endedRef = useRef(false)

  // Stabilize leave with useCallback so the socket isn't recreated on parent re-render
  const stableLeave = useCallback(() => leave(), [leave])

  const stopPolling = useCallback(() => {
    if (pollDelay.current) { clearTimeout(pollDelay.current); pollDelay.current = null }
    if (pollTimer.current) { clearInterval(pollTimer.current); pollTimer.current = null }
    pollAttempts.current = 0
  }, [])

  // The websocket delivers run.finished in the normal case. If it is missed (reconnect,
  // server restart, proxy hiccup), poll the submission endpoint instead of hanging forever.
  const startPolling = useCallback((code: string, submissionId: string) => {
    stopPolling()
    const check = async () => {
      try {
        const latest = await api.submission(code, submissionId)
        if (latest.status !== 'QUEUED' && latest.status !== 'RUNNING') {
          setRun(latest); setRunning(false)
          if (latest.testResults?.length) setOutputTab('tests')
          stopPolling()
          return
        }
      } catch {
        // The websocket may still deliver the result; retry until the polling cap.
      }
      pollAttempts.current += 1
      if (pollAttempts.current >= 40) {
        stopPolling(); setRunning(false)
        setError('Execution is taking longer than expected. Please try again.')
      }
    }
    pollDelay.current = setTimeout(() => {
      void check()
      pollTimer.current = setInterval(() => { void check() }, 1500)
    }, 15000)
  }, [stopPolling])

  useEffect(() => {
    if (!initial) return
    let alive = true
    Promise.all([api.room(initial.roomId), api.messages(initial.roomId), api.profile()]).then(([latest, chat, user]) => {
      if (!alive) return
      const parsedRoom = parseRoom(latest)
      setRoom(parsedRoom); setMessages(chat); setMe(user)
      // Load starter code from problem if no existing doc state
      if (parsedRoom.documentState) {
        setSource(decode(parsedRoom.documentState))
      } else if (parsedRoom.problem?.starterCode) {
        const sc = parsedRoom.problem.starterCode
        const lang = parsedRoom.language as keyof typeof sc
        setSource(sc[lang] ?? '')
      }
    }).catch(e => setError(e instanceof Error ? e.message : 'Unable to load room'))

    const live = new RoomSocket(initial.roomId, event => {
      if (event.type === 'participant.list') setRoom(current => current ? { ...current, participants: event.data as Room['participants'] } : current)
      if (event.type === 'permission.changed' || event.type === 'problem.changed' || event.type === 'language.changed') setRoom(current => event.data ? parseRoom(event.data as Room) : current)
      if (event.type === 'chat') setMessages(current => current.some(m => m.id === (event.data as ChatMessage).id) ? current : [...current, event.data as ChatMessage])
      if (event.type === 'doc.update' && (event.data as { payload?: string })?.payload) setSource(decode((event.data as { payload: string }).payload))
      if (event.type === 'doc.state' && (event.data as { payload?: string })?.payload) setSource(decode((event.data as { payload: string }).payload))
      if (event.type === 'run.started') setRunning(true)
      if (event.type === 'run.finished') {
        stopPolling(); setRunning(false)
        const result = event.data as RunResult
        setRun(result)
        if (result.testResults?.length) setOutputTab('tests')
      }
      if (event.type === 'session.ended' && !endedRef.current) { endedRef.current = true; stableLeave() }
    }, update => setSource(new TextDecoder().decode(update)))

    live.setOnStatus(setStatus)
    socket.current = live; live.connect()
    return () => { alive = false; live.close(); stopPolling() }
  }, [initial?.roomId, stableLeave, stopPolling])

  const role = useMemo(() => room?.participants.find(p => p.id === me?.id)?.role, [room, me])
  const isInterviewer = role === 'INTERVIEWER'
  const locked = !isInterviewer && (!!room?.editorLocked || !room?.candidateCanEdit)

  const problemExamples: ExampleItem[] = useMemo(() => {
    if (!room?.problem?.examples) return []
    if (Array.isArray(room.problem.examples)) return room.problem.examples
    if (typeof room.problem.examples === 'string') {
      try {
        const parsed = JSON.parse(room.problem.examples)
        return Array.isArray(parsed) ? parsed : []
      } catch {
        return []
      }
    }
    return []
  }, [room?.problem?.examples])

  if (!room) return <main className="page"><h1 className="text-2xl font-semibold">No room selected</h1><Button type="button" className="mt-5" onClick={stableLeave}>Back to dashboard</Button></main>

  const updateSource = (next: string) => { setSource(next); socket.current?.send('doc.update', { payload: encode(next) }) }
  const sendChat = async (event: React.FormEvent) => { event.preventDefault(); if (!draft.trim()) return; try { const created = await api.sendMessage(room.roomId, draft); setMessages(current => current.some(m => m.id === created.id) ? current : [...current, created]); setDraft('') } catch (e) { setError(e instanceof Error ? e.message : 'Could not send message') } }
  const execute = async (submit: boolean) => {
    setRunning(true); setError(''); setRun(null); stopPolling(); setOutputTab('output')
    try {
      const queued = submit
        ? await api.submit(room.roomId, { sourceCode: source, language: room.language })
        : await api.run(room.roomId, { sourceCode: source, language: room.language, stdin })
      if (queued?.id) startPolling(room.roomId, queued.id)
    } catch (e) {
      setRunning(false)
      setError(e instanceof Error ? e.message : 'Could not queue execution')
    }
  }
  const toggleLock = async () => { try { setRoom(parseRoom(await api.permissions(room.roomId, { editorLocked: !room.editorLocked }))) } catch (e) { setError(e instanceof Error ? e.message : 'Could not update permission') } }
  const changeLanguage = async (language: Room['language']) => {
    if (language === room.language) return
    const previousStarter = starterCodeFor(room, room.language)
    const replaceSource = source.trim().length === 0 || source === previousStarter
    setChangingLanguage(true); setError(''); setLanguageNotice('')
    try {
      const changed = parseRoom(await api.changeLanguage(room.roomId, language))
      setRoom(changed)
      if (replaceSource) {
        // A single collaboration update keeps the starter-code replacement atomic for peers.
        updateSource(starterCodeFor(changed, language))
      } else {
        setLanguageNotice(`Language changed to ${languageLabel[language]}. Your code was kept.`)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not change language')
    } finally {
      setChangingLanguage(false)
    }
  }
  const end = async () => { try { await api.endRoom(room.roomId, source); stableLeave() } catch (e) { setError(e instanceof Error ? e.message : 'Could not end session') } }

  const canChangeLanguage = isInterviewer || (role === 'CANDIDATE' && !locked)
  const languageChangeDisabledReason = room.status === 'ENDED'
    ? 'Session has ended'
    : locked
      ? 'The interviewer has locked candidate editing'
      : !canChangeLanguage
        ? 'Only room participants can change the language'
        : undefined

  return <main className="room">
    <div className="roombar"><div className="flex items-center gap-3"><strong>pair<span className="text-primary">code</span></strong><span className="text-muted-foreground">/</span><span className="font-medium">{room.name}</span><button type="button" className="room-id" title="Copy invite link" onClick={() => navigator.clipboard.writeText(`${window.location.origin}/join?room=${room.roomId}`)}>{room.roomId} <Copy /></button></div><div className="flex items-center gap-2"><select className="select" value={room.language} disabled={!canChangeLanguage || room.status === 'ENDED' || changingLanguage} title={languageChangeDisabledReason} onChange={event => changeLanguage(event.target.value as Room['language'])}><option>PYTHON</option><option>JAVA</option><option>CPP</option></select><Button type="button" size="sm" variant="secondary" disabled={running} onClick={() => execute(false)}><Play data-icon="inline-start" />{running ? 'Running' : 'Run'}</Button><Button type="button" size="sm" disabled={running} onClick={() => execute(true)}>Submit</Button>{isInterviewer && <Button type="button" size="sm" variant="destructive" onClick={end}>End session</Button>}</div></div>
    <div className="reconnect"><span className={`size-2 rounded-full ${status === 'connected' ? 'bg-emerald-400' : 'bg-amber-400'}`} /> {status === 'connected' ? 'Connected' : 'Reconnecting…'}</div>
    {error && <p className="mx-4 mt-3 text-sm text-destructive">{error}</p>}
    {languageNotice && <p className="mx-4 mt-3 text-sm text-muted-foreground" role="status">{languageNotice}</p>}
    <div className="room-grid"><section className="problem-pane"><div className="tabs"><button type="button" className="tab active">Problem</button></div><div className="p-5"><div className="flex items-center justify-between"><span className="text-xs text-muted-foreground">Problem {room.problem?.number ?? '—'}</span><span className={`badge ${(room.problem?.difficulty ?? 'EASY').toLowerCase()}`}>{room.problem?.difficulty ?? 'Unassigned'}</span></div><h1 className="mt-3 text-xl font-semibold">{room.problem?.title ?? 'No problem selected'}</h1><div className="mt-3"><span className="tag">{room.problem?.topic ?? '—'}</span></div><p className="mt-6 whitespace-pre-wrap text-sm leading-7 text-muted-foreground">{room.problem?.description ?? 'Select a problem to begin.'}</p>
      {problemExamples.length > 0 && <><h3 className="mt-7 text-sm font-semibold">Examples</h3>{problemExamples.map((ex, i) => <div key={i} className="mt-3 rounded bg-muted p-3 text-xs font-mono"><div><span className="font-semibold">Input:</span> {ex.input}</div><div><span className="font-semibold">Output:</span> {ex.output}</div>{ex.explanation && <div className="mt-1 text-muted-foreground">{ex.explanation}</div>}</div>)}</>}
      <h3 className="mt-7 text-sm font-semibold">Constraints</h3><p className="mt-3 whitespace-pre-wrap text-xs leading-6 text-muted-foreground">{room.problem?.constraints ?? '—'}</p></div></section>
      <section className="editor-pane"><div className="editor-top"><span>solution.{fileExtension[room.language]}</span><span className="text-xs text-muted-foreground">{locked ? 'Locked by interviewer' : 'Live sync enabled'}</span></div><div className={`editor ${locked ? 'locked' : ''}`}><textarea aria-label={`Solution code (${languageLabel[room.language]})`} className="min-h-[360px] w-full resize-none bg-transparent p-4 font-mono text-sm outline-none" data-editor-mode={editorMode[room.language]} spellCheck={false} value={source} readOnly={locked} onFocus={() => socket.current?.send('awareness', { payload: { state: 'editing', userId: me?.id } })} onChange={e => updateSource(e.target.value)} />{locked && <div className="lock-overlay"><Lock /><span>Editor locked by interviewer</span></div>}</div><div className="editor-footer"><span className="typing">{room.participants.filter(p => p.online).length} participants online</span>{isInterviewer && <Button type="button" variant="ghost" size="sm" onClick={toggleLock}>{room.editorLocked ? <Lock /> : <ShieldCheck />}{room.editorLocked ? 'Unlock editor' : 'Lock editor'}</Button>}</div><div className="output">
  <div className="tabs">
    <button type="button" className={`tab ${outputTab === 'output' ? 'active' : ''}`} onClick={() => setOutputTab('output')}>Output</button>
    <button type="button" className={`tab ${outputTab === 'tests' ? 'active' : ''}`} onClick={() => setOutputTab('tests')}>Test cases {run?.testResults && <span className="mini-count">{run.testResults.filter(x => x.passed).length}/{run.testResults.length}</span>}</button>
  </div>
  <div className="stdin">
    <label htmlFor="stdin">Standard input (used by Run)</label>
    <textarea id="stdin" value={stdin} onChange={e => setStdin(e.target.value)} placeholder="Type program input here…" spellCheck={false} />
  </div>
  <div className="output-body">
    {outputTab === 'output' ? (
      running ? <span className="text-sm text-muted-foreground">Running in sandbox…</span>
        : run ? <>
            <span className={`status ${statusClass[run.status] ?? 'pending'}`}><span />{statusLabel(run.status)}</span>
            <pre className="mt-3 max-h-28 overflow-auto whitespace-pre-wrap text-xs">{run.compileError || run.stderr || (run.status === 'SUCCESS' && !run.stdout?.trim() ? EMPTY_OUTPUT_MESSAGE : run.stdout || 'No output')}</pre>
            <p className="mt-2 text-xs text-muted-foreground">{run.executionTimeMs ?? 0} ms{run.score != null && ` · Score ${run.score}%`}{run.exitCode != null && run.exitCode !== 0 && ` · Exit code ${run.exitCode}`}</p>
          </>
        : <span className="text-sm text-muted-foreground">Run code to see output.</span>
    ) : !run?.testResults?.length ? (
      <span className="text-sm text-muted-foreground">Submit code to see test case results.</span>
    ) : (
      <div className="test-list">
        {run.testResults.map((result, index) => (
          <div className={`test-case ${result.passed ? 'passed' : 'failed'}`} key={result.testCaseId}>
            <div className="test-case-head">
              <span>{result.input === undefined ? `Hidden test ${index + 1}` : `Test case ${index + 1}`}</span>
              <span className={result.passed ? 'test-passed' : 'test-failed'}>{result.passed ? 'Passed' : 'Failed'} · {result.runtimeMs ?? 0} ms</span>
            </div>
            {result.input === undefined ? (
              <p className="text-xs text-muted-foreground">Hidden test data is not shown.</p>
            ) : (
              <>
                <div className="test-field"><span>Input</span><pre>{result.input}</pre></div>
                <div className="test-field"><span>Expected output</span><pre>{result.expectedOutput}</pre></div>
                <div className="test-field"><span>Actual output</span><pre>{result.actualOutput || '(no output)'}</pre></div>
              </>
            )}
          </div>
        ))}
      </div>
    )}
  </div>
</div></section>
      <aside className="chat-pane"><div className="tabs"><button type="button" className="tab active">Chat <span className="mini-count">{messages.length}</span></button><button type="button" className="tab"><Users className="inline size-3" /> {room.participants.length}</button></div><div className="p-4">{room.participants.map(p => <div className="mb-3 flex items-center gap-2" key={p.id}><span className={`size-2 rounded-full ${p.online ? 'bg-emerald-400' : 'bg-muted-foreground'}`} /><div><div className="text-sm font-medium">{p.name}</div><div className="text-xs text-muted-foreground">{p.role}</div></div></div>)}</div><div className="chat-log">{messages.map(m => <div className={`chat-message ${m.authorId === me?.id ? 'mine' : ''}`} key={m.id}><div className="text-[11px] text-muted-foreground">{m.author} · {new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div><div className="bubble">{m.content}</div></div>)}</div><form className="chat-input" onSubmit={sendChat}><input value={draft} onChange={e => setDraft(e.target.value)} placeholder="Message the room…" /><button type="submit" aria-label="Send"><Send /></button></form></aside></div>
  </main>
}
