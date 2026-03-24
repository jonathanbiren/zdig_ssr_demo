import type { WohnungDetail, WohnungListeEintrag } from '../model/types'

const BASE = '/api/katalog/wohnungen'

export async function fetchWohnungListe(): Promise<WohnungListeEintrag[]> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`Fehler beim Laden der Wohnungen: ${res.status}`)
  return res.json()
}

export async function fetchWohnungDetail(id: string): Promise<WohnungDetail> {
  const res = await fetch(`${BASE}/${id}`)
  if (res.status === 404) throw new Error('Wohnung nicht gefunden')
  if (!res.ok) throw new Error(`Fehler beim Laden der Wohnung: ${res.status}`)
  return res.json()
}
