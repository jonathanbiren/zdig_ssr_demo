package org.soldatenwohnen.core.katalog.domain.model

/**
 * The Kunde aggregate: a Kunde aggregate root together with its Verpflichtungen.
 * Loaded in a single round trip via jOOQ's MULTISET (see KundenRepository).
 */
class KundeMitVerpflichtungen(
	val kunde: Kunde,
	val verpflichtungen: List<Verpflichtung>,
)
