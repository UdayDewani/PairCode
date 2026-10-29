'use client'

import { FormEvent, useEffect, useState } from 'react'
import { ArrowRight, BookOpen, Check, Code2, LogOut, Play, Plus, Search, Settings, Sparkles, Users, Video } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { api, type Problem, type Room, type User } from '@/lib/api'
import { LiveRoom } from '@/components/live-room'

type Tab = 'dashboard' | 'problems' | 'sessions' | 'profile'
type Session = { id: string; roomId: string; name: string; status: string; language: string; score?: number; startedAt: string; endedAt?: string; durationSeconds?: number }

function Logo() { return <div className="flex items-center gap-2.5 font-semibold tracking-tight"><div className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground"><Code2 className="size-4" /></div><span>pair<span className="text-primary">code</span></span></div> }
function Empty({ children }: { children: React.ReactNode }) { return <div className="p-8 text-center text-sm text-muted-foreground">{children}</div> }
function Difficulty({ value }: { value: string }) { return <span className={`badge ${value.toLowerCase()}`}>{value}</span> }
function Avatar({ initials, color = 'bg-violet-500' }: { initials: string; color?: string }) { return <span className={`avatar ${color}`}>{initials}</span> }

function Landing({ onAuth }: { onAuth: (register?: boolean) => void }) {
  return (
    <main className="min-h-screen bg-grid">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
        <Logo />
        <div className="hidden items-center gap-7 text-sm text-muted-foreground md:flex">
          <a href="#features">Features</a>
          <a href="#how">How it works</a>
          <a href="#pricing">Pricing</a>
        </div>
        <div className="flex gap-2">
          <Button type="button" variant="ghost" onClick={() => onAuth(false)}>Log in</Button>
          <Button type="button" onClick={() => onAuth(true)}>Get started <ArrowRight data-icon="inline-end" className="size-4" /></Button>
        </div>
      </header>
      <section className="mx-auto grid max-w-6xl gap-16 px-6 pb-20 pt-20 lg:grid-cols-[1fr_1.05fr] lg:items-center">
        <div>
          <div className="eyebrow"><span className="pulse" /> Built for better interviews</div>
          <h1 className="mt-5 max-w-xl text-5xl font-semibold leading-[1.05] tracking-[-.04em] md:text-7xl">
            Interview.<br /><span className="text-primary">Code.</span> Together.
          </h1>
          <p className="mt-6 max-w-lg text-lg leading-8 text-muted-foreground">
            The collaborative coding workspace for technical interviews. Solve problems together, in real time, from anywhere.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button type="button" size="lg" onClick={() => onAuth(true)}>
              Start interviewing <ArrowRight data-icon="inline-end" className="size-4" />
            </Button>
            <Button type="button" size="lg" variant="outline" onClick={() => onAuth(false)}>
              View demo <Play data-icon="inline-start" className="size-4" />
            </Button>
          </div>
          <div className="mt-9 flex items-center gap-3 text-sm text-muted-foreground">
            <div className="flex -space-x-2">
              <Avatar initials="JD" color="bg-blue-500" />
              <Avatar initials="SK" color="bg-rose-500" />
              <Avatar initials="AM" color="bg-emerald-500" />
            </div>
            <span>Trusted by <strong className="text-foreground">2,000+</strong> engineering teams</span>
          </div>
        </div>
        <div className="product-frame">
          <div className="flex items-center justify-between border-b border-border/70 px-4 py-3">
            <div className="flex items-center gap-2">
              <span className="size-2 rounded-full bg-red-400" />
              <span className="size-2 rounded-full bg-amber-400" />
              <span className="size-2 rounded-full bg-emerald-400" />
            </div>
            <span className="text-[11px] text-muted-foreground">app.paircode.dev / room</span>
            <div className="size-4" />
          </div>
          <div className="flex h-[330px] text-left">
            <div className="w-[32%] border-r border-border/70 bg-muted/30 p-4">
              <div className="text-[10px] font-medium uppercase tracking-widest text-muted-foreground">Problem</div>
              <div className="mt-3 text-sm font-semibold">Two Sum</div>
              <Difficulty value="Easy" />
              <p className="mt-4 text-[11px] leading-5 text-muted-foreground">Given an array of integers, return indices of the two numbers that add up to a target.</p>
              <div className="mt-5 rounded-md border border-border/70 bg-background p-3 font-mono text-[10px] text-muted-foreground">
                Input: nums = [2,7,11,15]<br />Output: [0,1]
              </div>
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between border-b border-border/70 px-4 py-2 text-[10px] text-muted-foreground">
                <span>solution.py</span>
                <span className="rounded bg-emerald-500/10 px-2 py-1 text-emerald-400">● Priya is typing...</span>
              </div>
              <div className="code-preview p-4 text-[11px] leading-6">
                <span className="ln">1</span><span><i>def</i> twoSum(nums, target):</span><br />
                <span className="ln">2</span><span className="indent"><span className="blue">seen</span> = {'{}'}</span><br />
                <span className="ln">3</span><span className="indent"><i>for</i> i, num <span className="violet">in</span> enumerate(nums):</span><br />
                <span className="ln">4</span><span className="indent2"><span className="violet">if</span> target - num <span className="violet">in</span> seen:</span><br />
                <span className="ln">5</span><span className="indent3"><span className="violet">return</span> [seen[target-num], i]</span><br />
                <span className="ln">6</span><span className="indent"><span className="blue">seen</span>[num] = i</span><br />
                <span className="ln">7</span><span className="indent"> </span><span className="cursor-label">Uday</span>
              </div>
            </div>
          </div>
          <div className="flex items-center justify-between border-t border-border/70 bg-muted/20 px-4 py-2 text-[10px]">
            <span className="text-muted-foreground">2 participants online</span>
            <span className="text-emerald-400">✓ All tests passing</span>
          </div>
        </div>
      </section>
      <section id="features" className="mx-auto max-w-6xl border-t border-border/70 px-6 py-20">
        <div className="max-w-xl">
          <div className="eyebrow">Everything in one room</div>
          <h2 className="mt-4 text-3xl font-semibold tracking-tight">Designed for signal, not noise.</h2>
        </div>
        <div className="mt-10 grid gap-4 md:grid-cols-3">
          {[
            ['⌁', 'Live collaboration', 'See code changes, cursors, and notes as they happen.'],
            ['▶', 'Instant test results', 'Run against visible and hidden test cases in seconds.'],
            ['◈', 'A library that scales', 'Curated DSA problems for every level and interview loop.']
          ].map(([icon, title, desc]) => (
            <div className="feature-card" key={title}>
              <div className="feature-icon">{icon}</div>
              <h3 className="mt-5 font-medium">{title}</h3>
              <p className="mt-2 text-sm leading-6 text-muted-foreground">{desc}</p>
            </div>
          ))}
        </div>
      </section>
      <footer className="border-t border-border/70">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-7 text-sm text-muted-foreground">
          <Logo />
          <span>© 2026 PairCode. Built for builders.</span>
        </div>
      </footer>
    </main>
  )
}

function Auth({ initialRegister = false, onBack, complete }: { initialRegister?: boolean; onBack?: () => void; complete: (user: User) => void }) {
  const [register, setRegister] = useState(initialRegister), [name, setName] = useState(''), [email, setEmail] = useState(''), [password, setPassword] = useState(''), [error, setError] = useState(''), [busy, setBusy] = useState(false)
  const submit = async (event: FormEvent) => { event.preventDefault(); setBusy(true); setError(''); try { const result = register ? await api.register(name, email, password) : await api.login(email, password); complete(result.user) } catch (e) { setError(e instanceof Error ? e.message : 'Unable to authenticate') } finally { setBusy(false) } }
  return (
    <main className="auth-page">
      <div className="auth-card">
        <form className="auth-form" onSubmit={submit}>
          <div className="flex items-center justify-between">
            <Logo />
            {onBack && (
              <Button type="button" variant="ghost" size="sm" onClick={onBack}>
                Back to home
              </Button>
            )}
          </div>
          <div className="mt-12">
            <div className="eyebrow">{register ? 'Create your workspace' : 'Welcome back'}</div>
            <h1 className="mt-3 text-3xl font-semibold tracking-tight">{register ? 'Start interviewing better.' : 'Sign in to PairCode.'}</h1>
            <p className="mt-2 text-sm text-muted-foreground">{register ? 'Your next great hire starts with a shared room.' : 'Pick up where your last interview left off.'}</p>
            <div className="mt-8 flex flex-col gap-4">
              {register && <input className="input" required value={name} onChange={e => setName(e.target.value)} placeholder="Full name" />}
              <input className="input" required type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Work email" />
              <input className="input" required minLength={8} type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" />
              <Button type="submit" size="lg" className="w-full" disabled={busy}>{busy ? 'Please wait…' : register ? 'Create account' : 'Continue'}</Button>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <p className="mt-6 text-center text-sm text-muted-foreground">
              {register ? 'Already have an account?' : 'New to PairCode?'} <button type="button" className="font-medium text-primary" onClick={() => setRegister(!register)}>{register ? 'Log in' : 'Create an account'}</button>
            </p>
          </div>
        </form>
        <div className="auth-visual hidden md:grid">
          <div className="relative max-w-md">
            <Sparkles className="size-6 text-primary" />
            <h2 className="mt-6 text-4xl font-semibold leading-tight tracking-tight">The interview room, rethought.</h2>
            <p className="mt-4 leading-7 text-muted-foreground">Give candidates a great experience. Give interviewers the signal they need.</p>
          </div>
        </div>
      </div>
    </main>
  )
}

function CreateRoom({ close, opened, preferredLanguage }: { close: () => void; opened: (room: Room) => void; preferredLanguage: User['preferredLanguage'] }) {
  const [problems, setProblems] = useState<Problem[]>([]), [name, setName] = useState(''), [problemId, setProblemId] = useState(''), [language, setLanguage] = useState(preferredLanguage), [candidateCanEdit, setCandidateCanEdit] = useState(true), [error, setError] = useState(''), [busy, setBusy] = useState(false)
  useEffect(() => { api.problems().then(x => { setProblems(x.items); setProblemId(x.items[0]?.id ?? '') }).catch(e => setError(e.message)) }, [])
  const create = async (e: FormEvent) => { e.preventDefault(); setBusy(true); setError(''); try { opened(await api.createRoom({ name, problemId: problemId || undefined, language, candidateCanEdit })) } catch (e) { setError(e instanceof Error ? e.message : 'Could not create room') } finally { setBusy(false) } }
  return <div className="modal-backdrop"><form className="modal" onSubmit={create}><div className="eyebrow">New interview</div><h2 className="mt-2 text-xl font-semibold">Create a room</h2><div className="mt-6 flex flex-col gap-4"><label className="field">Room name<input className="input" required value={name} onChange={e => setName(e.target.value)} placeholder="e.g. Backend interview" /></label><label className="field">Problem<select className="select" value={problemId} onChange={e => setProblemId(e.target.value)}><option value="">No problem yet</option>{problems.map(p => <option key={p.id} value={p.id}>{p.title} · {p.difficulty}</option>)}</select></label><label className="field">Language<select className="select" value={language} onChange={e => setLanguage(e.target.value)}><option value="PYTHON">Python</option><option value="JAVA">Java</option><option value="CPP">C++</option></select></label><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={candidateCanEdit} onChange={e => setCandidateCanEdit(e.target.checked)} /> Candidate can edit</label>{error && <p className="text-sm text-destructive">{error}</p>}</div><div className="mt-7 flex justify-end gap-2"><Button type="button" variant="ghost" onClick={close}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Creating…' : 'Create room'}</Button></div></form></div>
}

function JoinRoom({ close, opened }: { close: () => void; opened: (room: Room) => void }) {
  const [code, setCode] = useState(''), [error, setError] = useState(''), [busy, setBusy] = useState(false)
  const join = async (e: FormEvent) => { e.preventDefault(); setBusy(true); setError(''); try { opened(await api.joinRoom(code.trim().toUpperCase())) } catch (e) { setError(e instanceof Error ? e.message : 'Could not join room') } finally { setBusy(false) } }
  return <div className="modal-backdrop"><form className="modal" onSubmit={join}><div className="eyebrow">Join interview</div><h2 className="mt-2 text-xl font-semibold">Join a room</h2><p className="mt-2 text-sm text-muted-foreground">Enter the room ID shared by the interviewer.</p><label className="field mt-6">Room ID<input className="input uppercase" autoFocus required value={code} onChange={e => setCode(e.target.value)} placeholder="PC-7X4K9" /></label>{error && <p className="mt-3 text-sm text-destructive">{error}</p>}<div className="mt-7 flex justify-end gap-2"><Button type="button" variant="ghost" onClick={close}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Joining…' : 'Join room'}</Button></div></form></div>
}

function Profile({ user, updated, failed }: { user: User; updated: (user: User) => void; failed: (message: string) => void }) { const [name, setName] = useState(user.name), [language, setLanguage] = useState(user.preferredLanguage), [busy, setBusy] = useState(false); const save = async (e: FormEvent) => { e.preventDefault(); setBusy(true); try { updated(await api.updateProfile({ name, preferredLanguage: language })) } catch (e) { failed(e instanceof Error ? e.message : 'Could not save profile') } finally { setBusy(false) } }; return <><div className="eyebrow">Account</div><h1 className="mt-2 text-3xl font-semibold">Profile & settings</h1><form className="panel mt-8 max-w-xl p-6" onSubmit={save}><label className="field">Full name<input className="input" value={name} onChange={e => setName(e.target.value)} /></label><label className="field mt-4">Email<input className="input" value={user.email} readOnly /></label><label className="field mt-4">Preferred language<select className="select" value={language} onChange={e => setLanguage(e.target.value as User['preferredLanguage'])}><option value="PYTHON">Python</option><option value="JAVA">Java</option><option value="CPP">C++</option></select></label><Button type="submit" className="mt-6" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</Button></form></> }

export default function Page() {
  const [user, setUser] = useState<User | null>(null), [loading, setLoading] = useState(true), [showAuth, setShowAuth] = useState(false), [authRegister, setAuthRegister] = useState(false), [tab, setTab] = useState<Tab>('dashboard'), [room, setRoom] = useState<Room | null>(null), [modal, setModal] = useState<'create' | 'join' | null>(null), [sessions, setSessions] = useState<Session[]>([]), [problems, setProblems] = useState<Problem[]>([]), [query, setQuery] = useState(''), [error, setError] = useState('')
  const load = async () => { try { const [me, recent, library] = await Promise.all([api.profile(), api.sessions(), api.problems()]); setUser(me); setSessions(recent as Session[]); setProblems(library.items) } catch (e) { if (api.isError401(e)) { api.logout(); setUser(null) } } finally { setLoading(false) } }
  useEffect(() => { if (api.token()) load(); else setLoading(false) }, [])
  if (loading) return <main className="auth-page"><span className="text-muted-foreground">Loading PairCode…</span></main>
  if (!user) {
    if (showAuth) {
      return (
        <Auth
          initialRegister={authRegister}
          onBack={() => setShowAuth(false)}
          complete={u => {
            setUser(u)
            setShowAuth(false)
            load()
          }}
        />
      )
    }
    return (
      <Landing
        onAuth={(register = false) => {
          setAuthRegister(register)
          setShowAuth(true)
        }}
      />
    )
  }
  if (room) return <LiveRoom room={room} leave={() => { setRoom(null); load() }} />
  const visibleProblems = problems.filter(p => `${p.title} ${p.topic}`.toLowerCase().includes(query.toLowerCase()))
  const recent = sessions.slice(0, 5), active = sessions.filter(s => s.status === 'ACTIVE').length
  const nav: Array<[Tab, string, React.ElementType]> = [['dashboard', 'Dashboard', Video], ['problems', 'Problems', BookOpen], ['sessions', 'Sessions', Users], ['profile', 'Profile & settings', Settings]]
  return <div className="app-shell"><aside className="sidebar"><div className="px-5 py-5"><Logo /></div><nav className="flex flex-col gap-1 px-3">{nav.map(([id, label, Icon]) => <button key={id} onClick={() => setTab(id)} className={`nav-item ${tab === id ? 'active' : ''}`}><Icon />{label}</button>)}</nav><div className="mt-auto p-4"><div className="flex items-center gap-3 border-t border-border/70 pt-4"><span className="avatar bg-amber-600">{user.name.slice(0, 2).toUpperCase()}</span><div className="min-w-0"><div className="truncate text-sm font-medium">{user.name}</div><div className="truncate text-xs text-muted-foreground">{user.email}</div></div><button className="ml-auto text-muted-foreground" title="Log out" onClick={() => { api.logout(); setUser(null); setShowAuth(false) }}><LogOut className="size-4" /></button></div></div></aside><div className="main-content"><header className="topbar"><div className="text-sm text-muted-foreground">Workspace / <span className="text-foreground">{tab === 'dashboard' ? 'Overview' : tab}</span></div></header><main className="page">{error && <p className="mb-4 text-sm text-destructive">{error}</p>}
    {tab === 'dashboard' && <><div className="flex flex-wrap items-start justify-between gap-4"><div><div className="eyebrow">{new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}</div><h1 className="mt-2 text-3xl font-semibold tracking-tight">Welcome, {user.name}.</h1><p className="mt-1 text-muted-foreground">Start an interview or join one with a room ID.</p></div><div className="flex gap-2"><Button variant="outline" onClick={() => setModal('join')}><Users data-icon="inline-start" />Join room</Button><Button onClick={() => setModal('create')}><Plus data-icon="inline-start" />Create room</Button></div></div><div className="mt-8 grid gap-4 md:grid-cols-3"><div className="stat-card"><Video className="stat-icon" /><div className="mt-5 text-2xl font-semibold">{sessions.length}</div><div className="mt-1 text-sm text-muted-foreground">Total sessions</div></div><div className="stat-card"><Check className="stat-icon" /><div className="mt-5 text-2xl font-semibold">{sessions.filter(s => s.status === 'ENDED').length}</div><div className="mt-1 text-sm text-muted-foreground">Completed sessions</div></div><div className="stat-card"><Users className="stat-icon" /><div className="mt-5 text-2xl font-semibold">{active}</div><div className="mt-1 text-sm text-muted-foreground">Active sessions</div></div></div><div className="panel mt-8"><div className="panel-header"><div><h2 className="font-semibold">Recent sessions</h2><p className="mt-1 text-xs text-muted-foreground">Your actual interview activity</p></div><Button variant="ghost" size="sm" onClick={() => setTab('sessions')}>View all</Button></div>{recent.length ? <div className="overflow-x-auto"><table><thead><tr><th>Room</th><th>Language</th><th>Started</th><th>Status</th></tr></thead><tbody>{recent.map(s => <tr key={s.id}><td className="font-medium">{s.name}<div className="mt-1 text-xs text-muted-foreground">{s.roomId}</div></td><td>{s.language}</td><td className="text-muted-foreground">{new Date(s.startedAt).toLocaleString()}</td><td><span className={`status ${s.status === 'ENDED' ? 'success' : 'warning'}`}><span />{s.status}</span></td></tr>)}</tbody></table></div> : <Empty>No sessions yet. Create a room or join one to get started.</Empty>}</div></>}
    {tab === 'problems' && <><div className="flex items-start justify-between"><div><div className="eyebrow">Practice library</div><h1 className="mt-2 text-3xl font-semibold">Problems</h1></div><Button onClick={() => setModal('create')}><Plus data-icon="inline-start" />Create room</Button></div><div className="search mt-8"><Search /><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search problems…" /></div><div className="panel mt-4 overflow-hidden">{visibleProblems.length ? <table><thead><tr><th>#</th><th>Problem</th><th>Topic</th><th>Difficulty</th><th>Acceptance</th></tr></thead><tbody>{visibleProblems.map(p => <tr key={p.id}><td>{p.number}</td><td className="font-medium">{p.title}</td><td>{p.topic}</td><td>{p.difficulty}</td><td>{p.acceptanceRate}%</td></tr>)}</tbody></table> : <Empty>No problems match your search.</Empty>}</div></>}
    {tab === 'sessions' && <><div className="eyebrow">Your activity</div><h1 className="mt-2 text-3xl font-semibold">Session history</h1><div className="panel mt-8 overflow-hidden">{sessions.length ? <table><thead><tr><th>Room</th><th>Language</th><th>Score</th><th>Status</th><th>Started</th></tr></thead><tbody>{sessions.map(s => <tr key={s.id}><td className="font-medium">{s.name}</td><td>{s.language}</td><td>{s.score ?? '—'}</td><td>{s.status}</td><td>{new Date(s.startedAt).toLocaleString()}</td></tr>)}</tbody></table> : <Empty>No sessions yet.</Empty>}</div></>}
    {tab === 'profile' && <Profile user={user} updated={setUser} failed={setError} />}
  </main></div>{modal === 'create' && <CreateRoom close={() => setModal(null)} opened={r => { setRoom(r); setModal(null) }} preferredLanguage={user.preferredLanguage} />}{modal === 'join' && <JoinRoom close={() => setModal(null)} opened={r => { setRoom(r); setModal(null) }} />}</div>
}
