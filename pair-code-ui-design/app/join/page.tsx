'use client'

import { Suspense, useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { api, type Room } from '@/lib/api'
import { LiveRoom } from '@/components/live-room'
import { Button } from '@/components/ui/button'

function JoinRoom() {
  const params = useSearchParams()
  const router = useRouter()
  const [room, setRoom] = useState<Room | null>(null)
  const [error, setError] = useState('')
  const roomCode = params.get('room')?.toUpperCase()
  useEffect(() => {
    if (!roomCode) { setError('A room code is required.'); return }
    api.joinRoom(roomCode).then(setRoom).catch(e => setError(e instanceof Error ? e.message : 'Unable to join room'))
  }, [roomCode])
  if (room) return <LiveRoom room={room} leave={() => router.push('/')} />
  return <main className="auth-page"><div className="auth-card"><div className="auth-form"><h1 className="text-2xl font-semibold">Join PairCode room</h1><p className="mt-3 text-muted-foreground">{error || `Joining ${roomCode ?? 'room'}…`}</p>{error && <Button className="mt-6" onClick={() => router.push('/')}>Back to PairCode</Button>}</div></div></main>
}

export default function JoinPage() { return <Suspense fallback={<main className="page">Joining room…</main>}><JoinRoom /></Suspense> }
