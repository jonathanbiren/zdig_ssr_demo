package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.zdigdemo.config.ClientSearchProperties
import org.zdigdemo.core.kunde.Kunde
import org.zdigdemo.jooq.routines.references.wordSimilarity
import org.zdigdemo.jooq.tables.records.KundenRecord
import org.zdigdemo.jooq.tables.references.KUNDEN
import java.util.*
import kotlin.enums.EnumEntries

/**
 * DAO API cannot express (joins, MULTISET aggregation, the trigram search) stays a
 * handwritten DSL query — that split is the intended jOOQ usage, not a workaround.
 *
 * DAO and DSL calls share Spring Boot's jOOQ Configuration, so both participate in
 * this class's transaction.
 */

@Transactional
@Repository
class KundenRepository(
	private val dsl: DSLContext,
	private val clientSearchProperties: ClientSearchProperties,
) {

	private val KUNDEN_SUCHSPALTE: Field<String?> = DSL.concat(
		KUNDEN.KURZNAME, DSL.inline(" "),
		KUNDEN.NAME, DSL.inline(" "),
		KUNDEN.ZS_NUMMER.cast(String::class.java), DSL.inline(" "),
		KUNDEN.ADRESSE, DSL.inline(" "),
		KUNDEN.STATUS
	)

	fun getAllClientsCount(): Int {
		return dsl.fetchCount(KUNDEN)
	}

	private fun getClientsCountFittingSearchterm(searchTerm: String): Int {
		val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHSPALTE)
		return dsl.fetchCount(
			KUNDEN,
			similarity.greaterOrEqual(clientSearchProperties.minimumSimilarity.toFloat())
		)
	}

	fun getClient(id: UUID): KundenRecord? =
		dsl.selectFrom(KUNDEN).where(KUNDEN.ID.eq(id))
			.fetchOne()

	//* Cascading Deletion for the Kunde's Verpflichtungen is done at the database level through [[V4__create_verpflichtungen.sql]]
	//* ON DELETE CASCADE.
	fun deleteClient(id: UUID) = dsl.deleteFrom(KUNDEN).where(KUNDEN.ID.eq(id)).execute()


	fun getClients(
		sortData: SortData = SortData(),
		searchTerm: String? = ""
	): ClientPage {
		val sortField = when (sortData.sortDirection) {
			SortDirection.ASC -> sortData.sortColumn.field.asc()
			SortDirection.DESC -> sortData.sortColumn.field.desc()
		}

		if (searchTerm.isNullOrEmpty()) {
			return ClientPage(
				data = dsl.selectFrom(KUNDEN)
					.orderBy(listOf(sortField, KUNDEN.ID))
					.limit(clientSearchProperties.pageSize)
					.fetch(),
				amountSearchResult = getAllClientsCount()
			)
		} else {
			val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHSPALTE)
			return ClientPage(
				data = dsl.selectFrom(KUNDEN)
					.where(similarity.greaterOrEqual(clientSearchProperties.minimumSimilarity.toFloat()))
					.orderBy(listOf(similarity.desc(), sortField, KUNDEN.ID))
					.limit(clientSearchProperties.pageSize)
					.fetch(),
				amountSearchResult = getClientsCountFittingSearchterm(searchTerm)
			)
		}
	}

	fun getNextClientPage(
		lastSeenId: UUID,
		sortData: SortData,
		searchTerm: String? = ""
	): List<KundenRecord> {
		val lastSeenClient = getClient(lastSeenId) ?: return emptyList()
		val lastSeenValue = requireNotNull(lastSeenClient.get(sortData.sortColumn.field))
		val sortField = when (sortData.sortDirection) {
			SortDirection.ASC -> sortData.sortColumn.field.asc()
			SortDirection.DESC -> sortData.sortColumn.field.desc()
		}

		if (searchTerm.isNullOrEmpty()) {
			return dsl.selectFrom(KUNDEN)
				.orderBy(listOf(sortField, KUNDEN.ID))
				.seek(lastSeenValue, lastSeenId)
				.limit(clientSearchProperties.pageSize)
				.fetch()
		} else {
			val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHSPALTE)
			val lastSeenSimilarity = requireNotNull(
				dsl.select(similarity)
					.from(KUNDEN)
					.where(KUNDEN.ID.eq(lastSeenId))
					.fetchOne(similarity)
			)

			return dsl.selectFrom(KUNDEN)
				.where(similarity.ge(clientSearchProperties.minimumSimilarity.toFloat()))
				.orderBy(listOf(similarity.desc(), sortField, KUNDEN.ID))
				.seek(lastSeenSimilarity, lastSeenValue, lastSeenId)
				.limit(clientSearchProperties.pageSize)
				.fetch()
		}
	}

	fun editExistingClient(kunde: Kunde): Kunde {
		dsl.update(KUNDEN)
			.set(KUNDEN.KURZNAME, kunde.kundenKurzname.value)
			.set(KUNDEN.NAME, kunde.kundenName.value)
			.set(KUNDEN.ZS_NUMMER, kunde.kundenZsNummer.value)
			.set(KUNDEN.ADRESSE, kunde.adresse)
			.set(KUNDEN.STATUS, kunde.status)
			.set(KUNDEN.BESONDERS_SCHUETZENSWERT, kunde.besondersSchuetzenswert)
			.where(KUNDEN.ID.eq(kunde.id))
			.execute()
		return kunde
	}
//endregion
}

data class ClientPage(val data: List<KundenRecord>, val amountSearchResult: Int)

//region Sorting
enum class SortDirection(val value: String) {
	ASC("asc"),
	DESC("desc");

	fun invert(): SortDirection {
		return when (this) {
			ASC -> DESC
			DESC -> ASC
		}
	}

	fun isAscending(): Boolean {
		return this == ASC
	}

	companion object {
		fun create(value: String?): SortDirection {
			return when (value) {
				"asc" -> SortDirection.ASC
				"desc" -> SortDirection.DESC
				else -> SortDirection.ASC
			}
		}
	}
}


//This is the allowList we will pass the parameter passed from the controller into to map it to the valid JOOQ field
enum class KundenSortColumn(
	val requestName: String,
	val viewName: String,
	val field: Field<*>
) {
	KURZNAME("kurzname", viewName = "Kurzname", KUNDEN.KURZNAME),
	NAME("name", viewName = "Name", KUNDEN.NAME),
	ZS_NUMMER("zsNummer", viewName = "ZS-NR", KUNDEN.ZS_NUMMER),
	ADRESSE("adresse", viewName = "Adresse", KUNDEN.ADRESSE),
	STATUS("status", viewName = "Status", KUNDEN.STATUS),
	IST_BESONDERS_SCHUETZENSWERT(
		"istBesondersSchuetzenswert",
		viewName = "Besonders Schützenswert", KUNDEN.BESONDERS_SCHUETZENSWERT
	);

	companion object {
		fun fromRequestName(requestName: String?): KundenSortColumn {
			if (requestName.isNullOrEmpty()) return KundenSortColumn.KURZNAME
			return entries.find { it.requestName == requestName }
				?: throw IllegalArgumentException("Invalid sort column: $requestName")
		}
	}
}

data class SortData(
	val sortColumn: KundenSortColumn = KundenSortColumn.KURZNAME,
	val sortDirection: SortDirection = SortDirection.ASC
) {
	companion object {
		val columnNames: EnumEntries<KundenSortColumn> = KundenSortColumn.entries
	}
}
//endregion

//region Mappers
// jOOQ's KotlinGenerator types every column as nullable, because any SQL expression can in
// principle yield NULL. All columns of kunden and verpflichtungen are declared NOT NULL in
// the schema (see src/main/resources/db/migration), so these non-null assertions are backed
// by the database itself. DAO methods return generated POJOs, DSL queries return Records —
// hence one mapper per generated type.


fun Kunde.toRecord(): KundenRecord = KundenRecord(
	id = id,
	kurzname = kundenKurzname.value,
	name = kundenName.value,
	zsNummer = kundenZsNummer.value,
	adresse = adresse,
	status = status,
	besondersSchuetzenswert = besondersSchuetzenswert
)
//endregion
