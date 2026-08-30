package org.soldatenwohnen.adapter.out

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Result
import org.jooq.impl.DSL
import org.soldatenwohnen.config.ClientSearchProperties
import org.soldatenwohnen.core.katalog.application.port.out.IKundenRepository
import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.soldatenwohnen.core.katalog.domain.model.KundeMitVerpflichtungen
import org.soldatenwohnen.jooq.routines.references.wordSimilarity
import org.soldatenwohnen.jooq.tables.daos.KundenDao
import org.soldatenwohnen.jooq.tables.pojos.Kunden
import org.soldatenwohnen.jooq.tables.records.KundenRecord
import org.soldatenwohnen.jooq.tables.records.VerpflichtungenRecord
import org.soldatenwohnen.jooq.tables.references.KUNDEN
import org.soldatenwohnen.jooq.tables.references.VERPFLICHTUNGEN
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Plain single-table CRUD goes through the generated [org.soldatenwohnen.jooq.tables.daos.KundenRepository]. Anything the fixed
 * DAO API cannot express (joins, MULTISET aggregation, the trigram search) stays a
 * hand-written DSL query — that split is the intended jOOQ usage, not a workaround.
 *
 * DAO and DSL calls share Spring Boot's jOOQ Configuration, so both participate in
 * this class's transaction.
 */
@Transactional
@Repository
class KundenRepository(
	private val dsl: DSLContext,
	// We inject the jOOQ Repository (DAO) which is a bean that we registered in the Spring context through our
	// config and can therefore be injected here by Spring. We can use it to greatly simplify single table CRUD
	// operations
	private val kundenDao: KundenDao,
	private val clientSearchProperties: ClientSearchProperties,
) : IKundenRepository {

	override fun getAllClients(): List<Kunde> =
		kundenDao.findAll().map { it.toDomain() }

	override fun getClient(id: UUID): Kunde? =
		kundenDao.findById(id)?.toDomain()

	override fun deleteClient(id: UUID) {
		// Children must be deleted first: verpflichtungen.kunde_id references kunden.id,
		// so deleting the Kunde while Verpflichtungen still exist violates the FK.
		dsl.deleteFrom(VERPFLICHTUNGEN)
			.where(VERPFLICHTUNGEN.KUNDE_ID.eq(id))
			.execute()
		kundenDao.deleteById(id)
	}

	/**
	 * Loads a whole aggregate — one Kunde plus all of its Verpflichtungen — in a single
	 * round trip. MULTISET nests the child rows as a typed collection; on PostgreSQL jOOQ
	 * renders this as a correlated jsonb_agg subquery and parses the result back into
	 * typed records. No N+1 queries, no ORM magic.
	 */
	override fun getKundeMitVerpflichtungen(id: UUID): KundeMitVerpflichtungen? {
		val verpflichtungenNested: Field<Result<VerpflichtungenRecord>> = DSL.multiset(
			DSL.selectFrom(VERPFLICHTUNGEN)
				.where(VERPFLICHTUNGEN.KUNDE_ID.eq(KUNDEN.ID))
				.orderBy(VERPFLICHTUNGEN.ERSTELLT_AM)
		)
		return dsl.select(
			KUNDEN.ID, KUNDEN.KURZNAME, KUNDEN.NAME, KUNDEN.ZS_NUMMER,
			KUNDEN.ADRESSE, KUNDEN.STATUS, KUNDEN.BESONDERS_SCHUETZENSWERT,
			verpflichtungenNested
		)
			.from(KUNDEN)
			.where(KUNDEN.ID.eq(id))
			.fetchOne()
			?.let { row ->
				KundeMitVerpflichtungen(
					// into(KUNDEN) copies the columns by name into a typed KundenRecord
					kunde = row.into(KUNDEN).toDomain(),
					verpflichtungen = row.get(verpflichtungenNested).map { it.toDomain() }
				)
			}
	}

	// Custom Query using Postgres specific word_similarity function
	override fun getAllClientsFittingSearchterm(searchTerm: String): List<Kunde> {
		// wordSimilarity is the pg_trgm function word_similarity(), reverse-engineered
		// by the code generator into a typed Kotlin function. No string SQL anywhere.
		val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHTEXT)
		return dsl.selectFrom(KUNDEN)
			.where(similarity.ge(clientSearchProperties.minimumSimilarity.toFloat()))
			.orderBy(similarity.desc())
			.limit(clientSearchProperties.maxAmountOfSearchResults)
			.fetch()
			.map { it.toDomain() }
	}
}

// The text the trigram search runs against: kurzname, name, zs_nummer, adresse, status
// concatenated with spaces, exactly like the previous hand-written @Query did with concat_ws.
private val KUNDEN_SUCHTEXT: Field<String?> = DSL.concat(
	KUNDEN.KURZNAME, DSL.inline(" "),
	KUNDEN.NAME, DSL.inline(" "),
	KUNDEN.ZS_NUMMER.cast(String::class.java), DSL.inline(" "),
	KUNDEN.ADRESSE, DSL.inline(" "),
	KUNDEN.STATUS
)

// jOOQ's KotlinGenerator types every column as nullable, because any SQL expression can in
// principle yield NULL. All columns of kunden and verpflichtungen are declared NOT NULL in
// the schema (see src/main/resources/db/migration), so these non-null assertions are backed
// by the database itself. DAO methods return generated POJOs, DSL queries return Records —
// hence one mapper per generated type.
fun Kunden.toDomain(): Kunde = Kunde(
	id = id!!,
	kurzname = kurzname!!,
	name = name!!,
	zsNummer = zsNummer!!,
	adresse = adresse!!,
	status = status!!,
	istBesondersSchuetzenswert = besondersSchuetzenswert!!
)

fun KundenRecord.toDomain(): Kunde = Kunde(
	id = id!!,
	kurzname = kurzname!!,
	name = name!!,
	zsNummer = zsNummer!!,
	adresse = adresse!!,
	status = status!!,
	istBesondersSchuetzenswert = besondersSchuetzenswert!!
)
