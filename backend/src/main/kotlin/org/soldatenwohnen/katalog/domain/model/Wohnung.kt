package org.soldatenwohnen.katalog.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ── Value Objects ─────────────────────────────────────────────────────────────

@JvmInline
value class WohnungId(val value: UUID) {
    companion object {
        fun neu(): WohnungId = WohnungId(UUID.randomUUID())
        fun von(value: String): WohnungId = WohnungId(UUID.fromString(value))
    }
}

// Mietpreis is stored in cents to avoid floating-point issues.
// The domain works in cents; the presentation layer converts to euros.
@JvmInline
value class Mietpreis(val centbetrag: Int) {
    init {
        require(centbetrag > 0) { "Mietpreis muss positiv sein" }
    }
    fun inEuro(): BigDecimal = centbetrag.toBigDecimal().divide(100.toBigDecimal())
}

data class Adresse(
    val strasse: String,
    val hausnummer: String,
    val postleitzahl: String,
    val stadt: String
)

enum class WohnungStatus {
    ENTWURF,
    VEROEFFENTLICHT,
    VERGEBEN
}

// ── Aggregate Root ────────────────────────────────────────────────────────────

// Wohnung is the aggregate root for the apartment listing concept.
// All access to apartment data goes through this class.
// For these two read-only use cases, there is no mutation, so we keep it simple.
data class Wohnung(
    val id: WohnungId,
    val titel: String,
    val beschreibung: String,
    val adresse: Adresse,
    val quadratmeter: BigDecimal,
    val zimmeranzahl: BigDecimal,
    val mietpreis: Mietpreis,
    val status: WohnungStatus,
    val erstelltAm: Instant
) {
    fun istVeroeffentlicht(): Boolean = this.status == WohnungStatus.VEROEFFENTLICHT
}
