package org.soldatenwohnen.core.katalog.domain.model

import java.time.OffsetDateTime
import java.util.UUID

class Verpflichtung(
	var id: UUID,
	var kundeId: UUID,
	var bezeichnung: String,
	var betragCent: Long,
	var erstelltAm: OffsetDateTime = OffsetDateTime.now(),
)
