package org.soldatenwohnen.adapter.out

import org.jooq.DSLContext
import org.soldatenwohnen.core.katalog.domain.model.Verpflichtung
import org.soldatenwohnen.jooq.tables.records.VerpflichtungenRecord
import org.soldatenwohnen.jooq.tables.references.VERPFLICHTUNGEN
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class VerpflichtungRepository(
	private val dsl: DSLContext,
) {

	fun findByKundeId(kundeId: UUID): List<Verpflichtung> =
		dsl.selectFrom(VERPFLICHTUNGEN)
			.where(VERPFLICHTUNGEN.KUNDE_ID.eq(kundeId))
			.orderBy(VERPFLICHTUNGEN.ERSTELLT_AM)
			.fetch()
			.map { it.toDomain() }

	fun findById(id: UUID): Verpflichtung? =
		dsl.selectFrom(VERPFLICHTUNGEN)
			.where(VERPFLICHTUNGEN.ID.eq(id))
			.fetchOne()
			?.toDomain()

	/**
	 * Upsert: INSERT ... ON CONFLICT (id) DO UPDATE. There is no dirty checking and no
	 * cascade magic in jOOQ — the SQL you read here is the SQL that runs, wrapped in
	 * whatever transaction the caller has opened (Spring's @Transactional works as usual).
	 */
	fun save(verpflichtung: Verpflichtung): Verpflichtung {
		dsl.insertInto(VERPFLICHTUNGEN)
			.set(VERPFLICHTUNGEN.ID, verpflichtung.id)
			.set(VERPFLICHTUNGEN.KUNDE_ID, verpflichtung.kundeId)
			.set(VERPFLICHTUNGEN.BEZEICHNUNG, verpflichtung.bezeichnung)
			.set(VERPFLICHTUNGEN.BETRAG_CENT, verpflichtung.betragCent)
			.set(VERPFLICHTUNGEN.ERSTELLT_AM, verpflichtung.erstelltAm)
			.onConflict(VERPFLICHTUNGEN.ID)
			.doUpdate()
			.set(VERPFLICHTUNGEN.BEZEICHNUNG, verpflichtung.bezeichnung)
			.set(VERPFLICHTUNGEN.BETRAG_CENT, verpflichtung.betragCent)
			.execute()
		return verpflichtung
	}

	fun deleteById(id: UUID) {
		dsl.deleteFrom(VERPFLICHTUNGEN)
			.where(VERPFLICHTUNGEN.ID.eq(id))
			.execute()
	}
}

// See KundenRecord.toDomain for why the non-null assertions are safe.
fun VerpflichtungenRecord.toDomain(): Verpflichtung = Verpflichtung(
	id = id!!,
	kundeId = kundeId!!,
	bezeichnung = bezeichnung!!,
	betragCent = betragCent!!,
	erstelltAm = erstelltAm!!
)
