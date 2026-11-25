package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SortField
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.zdigdemo.config.ObligationSearchProperties
import org.zdigdemo.jooq.routines.references.wordSimilarity
import org.zdigdemo.jooq.tables.records.VerpflichtungenRecord
import org.zdigdemo.jooq.tables.references.KUNDEN
import org.zdigdemo.jooq.tables.references.PERSONEN
import org.zdigdemo.jooq.tables.references.VERPFLICHTUNGEN
import java.time.OffsetDateTime
import java.util.*
import kotlin.enums.EnumEntries

/**
 * Repository (DAO) for single-table CRUD and DSLContext for everything that needs to be hand-rolled.
 * Uses the generated [VerpflichtungenDao] for by-id CRUD and hand-written DSL queries
 * where the fixed DAO API is not expressive enough.
 */
@Transactional
@Repository
class VerpflichtungRepository(
	private val dsl: DSLContext,
	private val obligationSearchProperties: ObligationSearchProperties,
) {
	private val VERPFLICHTUNGEN_SUCHSPALTE: Field<String?> = DSL.concat(
		VERPFLICHTUNGEN.BEZEICHNUNG,
		DSL.inline(" "),
		VERPFLICHTUNGEN.BETRAG_CENT.cast(String::class.java),
		DSL.inline(" "),
		KUNDEN.NAME,
		DSL.inline(" "),
		PERSONEN.VORNAME,
		DSL.inline(" "),
		PERSONEN.NACHNAME
	)

	fun getAllVerpflichtungenCount(): Int =
		dsl.fetchCount(VERPFLICHTUNGEN)

	private fun getVerpflichtungenCountFittingSearchterm(searchTerm: String): Int {
		val searchCondition = if (searchTerm.length < 3) {
			VERPFLICHTUNGEN_SUCHSPALTE.containsIgnoreCase(searchTerm)
		} else {
			val similarity = wordSimilarity(DSL.`val`(searchTerm), VERPFLICHTUNGEN_SUCHSPALTE)
			similarity.greaterOrEqual(obligationSearchProperties.minimumSimilarity.toFloat())
		}

		return dsl.fetchCount(
			dsl.select(VERPFLICHTUNGEN.ID)
				.from(VERPFLICHTUNGEN)
				.join(KUNDEN).on(KUNDEN.ID.eq(VERPFLICHTUNGEN.KUNDE_ID))
				.join(PERSONEN).on(PERSONEN.ID.eq(VERPFLICHTUNGEN.PERSON_ID))
				.where(searchCondition)
		)
	}

	private fun getVerpflichtungenSortField(
		sortierInformation: VerpflichtungSortData,
	) =
		when (sortierInformation.sortDirection) {
			SortDirection.ASC -> sortierInformation.sortColumn.field.asc()
			SortDirection.DESC -> sortierInformation.sortColumn.field.desc()
		}

	private fun verpflichtungenBaseQuery() =
		dsl.select(
			VERPFLICHTUNGEN.ID,
			VERPFLICHTUNGEN.KUNDE_ID,
			VERPFLICHTUNGEN.PERSON_ID,
			KUNDEN.NAME,
			PERSONEN.VORNAME,
			PERSONEN.NACHNAME,
			VERPFLICHTUNGEN.BEZEICHNUNG,
			VERPFLICHTUNGEN.BETRAG_CENT,
		)
			.from(VERPFLICHTUNGEN)
			.join(KUNDEN).on(KUNDEN.ID.eq(VERPFLICHTUNGEN.KUNDE_ID))
			.join(PERSONEN).on(PERSONEN.ID.eq(VERPFLICHTUNGEN.PERSON_ID))

	private fun verpflichtungenQuery(sucheingabe: String?) =
		if (sucheingabe.isNullOrEmpty()) {
			verpflichtungenBaseQuery()
		} else {
			verpflichtungenBaseQuery()
				.where(VERPFLICHTUNGEN_SUCHSPALTE.containsIgnoreCase(sucheingabe))
		}

	private fun getVerpflichtungenCount(sucheingabe: String?): Int =
		if (sucheingabe.isNullOrEmpty()) {
			getAllVerpflichtungenCount()
		} else {
			getVerpflichtungenCountFittingSearchterm(sucheingabe)
		}

	fun getVerpflichtungen(
		sortierInformation: VerpflichtungSortData = VerpflichtungSortData(),
		sucheingabe: String? = "",
	): VerpflichtungPage {
		val sortField = getVerpflichtungenSortField(sortierInformation)

		if (sucheingabe.isNullOrEmpty()) {
			return VerpflichtungPage(
				data = verpflichtungenBaseQuery()
					.orderBy(listOf(sortField, VERPFLICHTUNGEN.ID))
					.limit(obligationSearchProperties.pageSize)
					.fetch()
					.map { it.toVerpflichtungViewModel2() },
				amountSearchResult = getAllVerpflichtungenCount()
			)
		}

		if (sucheingabe.length < 3) {
			return VerpflichtungPage(
				data = verpflichtungenBaseQuery()
					.where(VERPFLICHTUNGEN_SUCHSPALTE.containsIgnoreCase(sucheingabe))
					.orderBy(listOf(sortField, VERPFLICHTUNGEN.ID))
					.limit(obligationSearchProperties.pageSize)
					.fetch()
					.map { it.toVerpflichtungViewModel2() },
				amountSearchResult = getVerpflichtungenCountFittingSearchterm(sucheingabe)
			)
		}

		val similarity = wordSimilarity(DSL.`val`(sucheingabe), VERPFLICHTUNGEN_SUCHSPALTE)

		return VerpflichtungPage(
			data = verpflichtungenBaseQuery()
				.where(similarity.greaterOrEqual(obligationSearchProperties.minimumSimilarity.toFloat()))
				.orderBy(listOf(similarity.desc(), sortField, VERPFLICHTUNGEN.ID))
				.limit(obligationSearchProperties.pageSize)
				.fetch()
				.map { it.toVerpflichtungViewModel2() },
			amountSearchResult = getVerpflichtungenCountFittingSearchterm(sucheingabe)
		)
	}

	fun getNextVerpflichtungenPage(
		lastSeenId: UUID,
		sortierInformation: VerpflichtungSortData,
		sucheingabe: String? = "",
	): List<org.zdigdemo.edge.controller.VerpflichtungViewModel2> {
		val lastSeenSortValue = getLastSeenVerpflichtungSortValue(
			lastSeenId = lastSeenId,
			sortierInformation = sortierInformation
		) ?: return emptyList()

		val sortField = getVerpflichtungenSortField(sortierInformation)

		if (sucheingabe.isNullOrEmpty()) {
			return verpflichtungenBaseQuery()
				.orderBy(listOf(sortField, VERPFLICHTUNGEN.ID))
				.seek(lastSeenSortValue, lastSeenId)
				.limit(obligationSearchProperties.pageSize)
				.fetch()
				.map { it.toVerpflichtungViewModel2() }
		}

		if (sucheingabe.length < 3) {
			return verpflichtungenBaseQuery()
				.where(VERPFLICHTUNGEN_SUCHSPALTE.containsIgnoreCase(sucheingabe))
				.orderBy(listOf(sortField, VERPFLICHTUNGEN.ID))
				.seek(lastSeenSortValue, lastSeenId)
				.limit(obligationSearchProperties.pageSize)
				.fetch()
				.map { it.toVerpflichtungViewModel2() }
		}

		val similarity = wordSimilarity(DSL.`val`(sucheingabe), VERPFLICHTUNGEN_SUCHSPALTE)
		val lastSeenSimilarity = dsl.select(similarity)
			.from(VERPFLICHTUNGEN)
			.join(KUNDEN).on(KUNDEN.ID.eq(VERPFLICHTUNGEN.KUNDE_ID))
			.join(PERSONEN).on(PERSONEN.ID.eq(VERPFLICHTUNGEN.PERSON_ID))
			.where(VERPFLICHTUNGEN.ID.eq(lastSeenId))
			.fetchOne(similarity) ?: return emptyList()

		return verpflichtungenBaseQuery()
			.where(similarity.greaterOrEqual(obligationSearchProperties.minimumSimilarity.toFloat()))
			.orderBy(listOf(similarity.desc(), sortField, VERPFLICHTUNGEN.ID))
			.seek(lastSeenSimilarity, lastSeenSortValue, lastSeenId)
			.limit(obligationSearchProperties.pageSize)
			.fetch()
			.map { it.toVerpflichtungViewModel2() }
	}

	private fun getLastSeenVerpflichtungSortValue(
		lastSeenId: UUID,
		sortierInformation: VerpflichtungSortData
	): Any? =
		dsl.select(sortierInformation.sortColumn.field)
			.from(VERPFLICHTUNGEN)
			.join(KUNDEN).on(KUNDEN.ID.eq(VERPFLICHTUNGEN.KUNDE_ID))
			.join(PERSONEN).on(PERSONEN.ID.eq(VERPFLICHTUNGEN.PERSON_ID))
			.where(VERPFLICHTUNGEN.ID.eq(lastSeenId))
			.fetchOne(sortierInformation.sortColumn.field)

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
			.fetch().map { it.toVerpflichtungViewModel() }
	//endregion
}


data class VerpflichtungPage(
	val data: List<org.zdigdemo.edge.controller.VerpflichtungViewModel2>,
	val amountSearchResult: Int
)

class VerpflichtungViewModel(
    var id: UUID,
    var kundeId: UUID,
    var bezeichnung: String,
    var betragCent: Long,
    var erstelltAm: OffsetDateTime = OffsetDateTime.now(),
)

/*fun VerpflichtungViewModel.toDao() = Verpflichtungen(
	id = this.id,
	kundeId = this.kundeId,
	bezeichnung = this.bezeichnung,
	betragCent = this.betragCent,
	erstelltAm = this.erstelltAm
)*/

private fun Record.toVerpflichtungViewModel2(): org.zdigdemo.edge.controller.VerpflichtungViewModel2 =
	org.zdigdemo.edge.controller.VerpflichtungViewModel2(
		id = get(VERPFLICHTUNGEN.ID)!!.toString(),
		kundeId = get(VERPFLICHTUNGEN.KUNDE_ID)!!.toString(),
		kundeName = get(KUNDEN.NAME)!!,
		verpflichtungName = get(VERPFLICHTUNGEN.BEZEICHNUNG)!!,
		verpflichtungBetrag = get(VERPFLICHTUNGEN.BETRAG_CENT)!!.toString(),
		personId = get(VERPFLICHTUNGEN.PERSON_ID)!!.toString(),
		personName = "${get(PERSONEN.VORNAME)!!} ${get(PERSONEN.NACHNAME)!!}"
	)

data class VerpflichtungSortData(
	val sortColumn: VerpflichtungSortColumn = VerpflichtungSortColumn.BEZEICHNUNG,
	val sortDirection: SortDirection = SortDirection.ASC
) {
	companion object {
		val columnNames: EnumEntries<VerpflichtungSortColumn> = VerpflichtungSortColumn.entries
	}
}

enum class VerpflichtungSortColumn(
    val requestName: String,
    val viewName: String,
    val field: Field<*>
) {
    BEZEICHNUNG("bezeichnung", "Bezeichnung", VERPFLICHTUNGEN.BEZEICHNUNG),
    KUNDENNAME("kundenname", "Kundenname", KUNDEN.NAME),
    PERSONENNAME("personenname", "Person", PERSONEN.NACHNAME),
    BETRAG("betrag", "Betrag", VERPFLICHTUNGEN.BETRAG_CENT);

    companion object {
        fun fromRequestName(requestName: String?): VerpflichtungSortColumn {
            if (requestName.isNullOrEmpty()) return BEZEICHNUNG
            return entries.find { it.requestName == requestName }
                ?: throw IllegalArgumentException("Invalid sort column name: $requestName")
        }
    }
}



fun VerpflichtungenRecord.toVerpflichtungViewModel(): VerpflichtungViewModel = VerpflichtungViewModel(
    id = this.id!!,
    kundeId = this.kundeId!!,
    bezeichnung = this.bezeichnung!!,
    betragCent = this.betragCent!!,
    erstelltAm = this.erstelltAm!!
)
//endregion
