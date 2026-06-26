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
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ── Spring Data JDBC persistence model ───────────────────────────────────────

/* This is a separate class from the domain model.
 * It maps 1:1 to a database table that was created by a Flyway migration (V1__create_wohnungen.sql).
 * The @Table annotation does NOT create the table -- it only tells Spring Data JDBC which table to
 * read from and write to. The table must already exist (Flyway ensures it does on startup).
 * The domain model knows nothing about this class.
 *
 * Spring Data JDBC uses this class as the aggregate root for persistence. If it had child entities
 * (collections of other @Table-annotated classes), Spring Data JDBC would load, save, and delete
 * the entire object graph as one unit through this root.
 * You can basically set up a lot of the behavior of the repository in these data classes, which represent the
 * output model of the database.
 *
 */

// The Kotlin data class is just a mapping layer. It does not influence or enforce database-level constraints.
// The table is created through Flyway database migrations.
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
	// Is this where you define constraints of the table or do you have to manually define them in the database?
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
// Why only an interface? Spring auto-generates the full implementation at startup -- you never write a class.
// CrudRepository<T, ID> tells Spring Data JDBC: T is the aggregate root type (WohnungRow),
// ID is the primary key type (UUID). It does NOT create a table -- the table must already exist via Flyway.
// The interface gives you findById, save, delete, existsById, findAll, count, and more for free.

@Repository
interface WohnungJdbcRepository : CrudRepository<WohnungRow, UUID> {

	// You can write custom Queries through the use of the @Query annotation
	// The index on (status, erstellt_am DESC) in V1 migration makes this fast.
	@Query("SELECT * FROM wohnungen WHERE status = 'VEROEFFENTLICHT' ORDER BY erstellt_am DESC ")
	fun findAlleVeroeffentlichten(): List<WohnungRow>


}

// ── Adapter: implements the outbound port ────────────────────────────────────
// Bridges between the application's WohnungRepository interface. WohnungRepositoryAdapter implements the outbound port
// and uses the WohnungJdbcRepository to access the database. Maybe this is too much of an abstraction?


// This is the repository which implements the outbound port of the application core. It has the JDBC repository
// injected into it to which it relays its calls. Is this necessary or can you directly use the WohnungJdbcRepository?
@Component
class WohnungRepositoryAdapter(
	private val jdbcRepository: WohnungJdbcRepository
) : WohnungRepository {

	// Your repository adapter is auto-wired to a Spring data jdbc repository to which it simply relays the calls
	override fun findAlleVeroeffentlichten(): List<Wohnung> =
		jdbcRepository.findAlleVeroeffentlichten().map { it.toDomain() }

	override fun findById(id: WohnungId): Wohnung? {
		return jdbcRepository.findById(id.value).orElse(null)?.toDomain()
	}

}
