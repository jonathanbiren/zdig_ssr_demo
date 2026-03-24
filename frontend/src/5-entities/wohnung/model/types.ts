// Types mirror exactly what the backend API returns.
// They live in the entity layer because Wohnung is a core domain concept
// that multiple features and pages will reference.

export interface WohnungListeEintrag {
  id: string
  titel: string
  stadt: string
  postleitzahl: string
  quadratmeter: number
  zimmeranzahl: number
  mietpreisEuro: number
  erstelltAm: string
}

export interface WohnungDetail {
  id: string
  titel: string
  beschreibung: string
  strasse: string
  hausnummer: string
  postleitzahl: string
  stadt: string
  quadratmeter: number
  zimmeranzahl: number
  mietpreisEuro: number
  erstelltAm: string
}
