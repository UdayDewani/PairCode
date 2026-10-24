'use client'

import { FormEvent, Suspense, useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { api, type Room, type User } from '@/lib/api'
import { LiveRoom } from '@/components/live-room'
import { Button } from '@/components/ui/button'

function Auth({ onAuth }: { onAuth: (user: User) => void }) {
  const [register, setRegister] = useState(false), [name, setName] = useState(''), [email, setEmail] = useState(''), [password, setPassword] = useState(''), [error, setError] = useState(''), [busy, setBusy] = useState(false)
  const submit = async (e: FormEvent) => { e.preventDefault(); setBusy(true); setError(''); try { const r = register ? await api.register(name, email, password) : await api.login(email, password); onAuth(r.user) } catch (ex) { setError(ex instanceof Error ? ex.message : 'Unable to authenticate') } finally { setBusy(false) } }
  return (
    <main className="auth-page"><div className="auth-card"><form className="auth-form" onSubmit={submit}>
      <h1 className="text-2xl font-semibold">{register ? 'Create account to join' : 'Sign in to join room'}</h1>
      <div className="mt-6 flex flex-col gap-4">
        {register && <input className="input" required value={name} onChange={e => setName(e.target.value)} placeholder="Full name" />}
        <input className="input" required type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Work email" />
        <input className="input" required minLength={8} type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" />
        <Button type="submit" size="lg" className="w-full" disabled={busy}>{busy ? 'Please wait…' : register ? 'Create account' : 'Continue'}</Button>
        {error && <p className="text-sm text-destructive">{error}</p>}
      </div>
      <p className="mt-4 text-center text-sm text-muted-foreground">{register ? 'Already have an account?' : 'New to PairCode?'} <button type="button" className="font-medium text-primary" onClick={() => setRegister(!register)}>{register ? 'Log in' : 'Create an account'}</button></p>
    </form></div></main>
  )
}

function JoinRoom() {
  const params = useSearchParams()
  const router = useRouter()
  const [room, setRoom] = useState<Room | null>(null)
  const [error, setError] = useState('')
  const [authed, setAuthed] = useState(!!api.token())
  const roomCode = params.get('room')?.toUpperCase()

  useEffect(() => {
    if (!authed || !roomCode) { if (!roomCode) setError('A room code is required.'); return }
    api.joinRoom(roomCode).then(setRoom).catch(e => setError(e instanceof Error ? e.message : 'Unable to join room'))
  }, [authed, roomCode])

  if (!authed) return <Auth onAuth={() => setAuthed(true)} />
  if (room) return <LiveRoom room={room} leave={() => router.push('/')} />
  return (
    <main className="auth-page"><div className="auth-card"><div className="auth-form">
      <h1 className="text-2xl font-semibold">Join PairCode room</h1>
      <p className="mt-3 text-muted-foreground">{error || `Joining ${roomCode ?? 'room'}…`}</p>
      {error && <Button type="button" className="mt-6" onClick={() => router.push('/')}>Back to PairCode</Button>}
    </div></div></main>
  )
}

export default function JoinPage() { return <Suspense fallback={<main className="page">Joining room…</main>}><JoinRoom /></Suspense> }
