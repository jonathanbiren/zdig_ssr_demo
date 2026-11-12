package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.zdigdemo.jooq.tables.records.VerpflichtungenRecord
import org.zdigdemo.jooq.tables.references.VERPFLICHTUNGEN
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository (DAO) for single-table CRUD and DSLContext for everything that needs to be hand-rolled.
 * Uses the generated [VerpflichtungenDao] for by-id CRUD and hand-written DSL queries
 * where the fixed DAO API is not expressive enough.
 */
@Transactional
@Repository
class VerpflichtungRepository(
	private val dsl: DSLContext,
) {
	// region DAO methods
	// You should investigate whether it is a better practice to directly return the type of the DAO from
	// the domain layer or to map the DAO type to the domain layer first. Intuitively, given that we don't want
	// to have dependencies flowing from domain -> data access, I would say mapping it is the better choice.
	fun findById(id: UUID): VerpflichtungenRecord? =
		dsl.selectFrom(VERPFLICHTUNGEN).where(VERPFLICHTUNGEN.ID.eq(id)).fetchOne()

	// The DAO API only offers separate insert()/update(); an upsert (INSERT .. ON CONFLICT
	// .. DO UPDATE) is beyond it, so this stays a DSL query as well.
/*	fun save(verpflichtung: VerpflichtungViewModel): VerpflichtungViewModel {
		verpflichtungenDao.insert(verpflichtung.toDao())
		return verpflichtung
	}*/

	fun deleteById(id: UUID) {
		dsl.deleteFrom(VERPFLICHTUNGEN).where(VERPFLICHTUNGEN.ID.eq(id)).execute()
	}
	//endregion

	//region DSL Methods
	// The DAO fetcher (fetchByKundeId) cannot ORDER BY, but chronological order is part
	// of this query's contract — so it stays a DSL query.
	// Furthermore we have very basic (essentially 1 to 1 mapping) from 'VerpflichtungenRecord' (generated jOOQ type)
	// into 'Verpflichtung' (domain type), which is why we use '.into()'. For more complex mapping, you will need
	// to write an explicit mapper yourself.
	fun findByKundeId(kundeId: UUID): List<VerpflichtungViewModel> =
		dsl.selectFrom(VERPFLICHTUNGEN)
			.where(VERPFLICHTUNGEN.KUNDE_ID.eq(kundeId))
			.orderBy(VERPFLICHTUNGEN.ERSTELLT_AM)
			.fetch().map { it.toViewModel() }
	//endregion


}

class VerpflichtungViewModel(
	var id: UUID,
	var kundeId: UUID,
	var bezeichnung: String,
	var betragCent: Long,
	var erstelltAm: OffsetDateTime = OffsetDateTime.now(),
)

//region Mappers
// See KundenRepository for why the non-null assertions are safe, and why there is one
// mapper per generated type (POJO for the DAO, Record for DSL queries).

/*fun VerpflichtungViewModel.toDao() = Verpflichtungen(
	id = this.id,
	kundeId = this.kundeId,
	bezeichnung = this.bezeichnung,
	betragCent = this.betragCent,
	erstelltAm = this.erstelltAm
)*/



fun VerpflichtungenRecord.toViewModel(): VerpflichtungViewModel = VerpflichtungViewModel(
	id = this.id!!,
	kundeId = this.kundeId!!,
	bezeichnung = this.bezeichnung!!,
	betragCent = this.betragCent!!,
	erstelltAm = this.erstelltAm!!
)
//endregion
