package org.soldatenwohnen.core.katalog.domain.model

import java.util.UUID

class Kunde(
	var id: UUID,
	var kurzname: String,
	var name: String,
	var zsNummer: Long,
	var adresse: String,
	var status: String,
	var istBesondersSchuetzenswert: Boolean,
) {}