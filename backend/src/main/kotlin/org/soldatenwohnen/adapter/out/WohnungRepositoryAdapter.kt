package org.soldatenwohnen.adapter.out

import org.soldatenwohnen.core.katalog.application.port.out.WohnungRepository
import org.soldatenwohnen.core.katalog.domain.model.Adresse
import org.soldatenwohnen.core.katalog.domain.model.Mietpreis
import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId
import org.soldatenwohnen.core.katalog.domain.model.WohnungStatus
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ── Spring Data JDBC persistence model ───────────────────────────────────────
// This is a separate class from the domain model.
// It maps 1:1 to the database table. The domain model knows nothing about it.

@Table("wohnungen")
data class WohnungRow(
    @Id val id: UUID,
    val titel: String,
    val beschreibung: String,
    val strasse: String,
    val hausnummer: String,
    val postleitzahl: String,
    val stadt: String,
    val quadratmeter: BigDecimal,
    val zimmeranzahl: BigDecimal,
    val mietpreisCent: Int,
    val status: String,
    val erstelltAm: Instant
) {
    // Maps the database row back to the rich domain model.
    fun toDomain(): Wohnung = Wohnung(
        id = WohnungId(id),
        titel = titel,
        beschreibung = beschreibung,
        adresse = Adresse(
            strasse = strasse,
            hausnummer = hausnummer,
            postleitzahl = postleitzahl,
            stadt = stadt
        ),
        quadratmeter = quadratmeter,
        zimmeranzahl = zimmeranzahl,
        mietpreis = Mietpreis(mietpreisCent),
        status = WohnungStatus.valueOf(status),
        erstelltAm = erstelltAm
    )
}

// ── Spring Data JDBC repository interface ────────────────────────────────────
// Spring auto-generates the implementation at startup.

@Repository
interface WohnungJdbcRepository : CrudRepository<WohnungRow, UUID> {

    // The index on (status, erstellt_am DESC) in V1 migration makes this fast.
    @Query("SELECT * FROM wohnungen WHERE status = 'VEROEFFENTLICHT' ORDER BY erstellt_am DESC")
    fun findAlleVeroeffentlichten(): List<WohnungRow>
}

// ── Adapter: implements the outbound port ────────────────────────────────────
// Bridges between the application's WohnungRepository interface
// and the Spring Data JDBC implementation above.

@Component
class WohnungRepositoryAdapter(
    private val jdbcRepository: WohnungJdbcRepository
) : WohnungRepository {

    override fun findAlleVeroeffentlichten(): List<Wohnung> =
        jdbcRepository.findAlleVeroeffentlichten().map { it.toDomain() }

    override fun findById(id: WohnungId): Wohnung? =
        jdbcRepository.findById(id.value).orElse(null)?.toDomain()
}
