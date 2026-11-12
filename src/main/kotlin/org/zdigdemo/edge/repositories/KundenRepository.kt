package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.zdigdemo.config.ClientSearchProperties
import org.zdigdemo.edge.controller.KundeDTO
import org.zdigdemo.edge.controller.KundeViewModel
import org.zdigdemo.jooq.routines.references.wordSimilarity
import org.zdigdemo.jooq.tables.daos.KundenDao
import org.zdigdemo.jooq.tables.pojos.Kunden
import org.zdigdemo.jooq.tables.records.KundenRecord
import org.zdigdemo.jooq.tables.references.KUNDEN
import java.util.*
import kotlin.enums.EnumEntries

/**
 * Plain single-table CRUD goes through the generated [org.zdigdemo.jooq.tables.daos.KundenDao]. Anything the fixed
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
    private val kundenDao: KundenDao,
    private val clientSearchProperties: ClientSearchProperties,
) {

    //region DAO Methods
    fun getAllClients(): List<KundeViewModel> {
        return kundenDao.findAll().map { kundeDao -> kundeDao.toViewModel() }
    }

    fun getClient(id: UUID): KundeViewModel? =
        kundenDao.findById(id)?.toViewModel()

	fun getClientDTO(id: UUID): KundeDTO? =
		kundenDao.findById(id)?.toDTO
    //* Cascading Deletion for the Kunde's Verpflichtungen is done at the database level through [[V4__create_verpflichtungen.sql]]
    //* ON DELETE CASCADE.
    fun deleteClient(id: UUID) = kundenDao.deleteById(id)
    //endregion

    private val KUNDEN_SUCHTEXT: Field<String?> = DSL.concat(
        KUNDEN.KURZNAME, DSL.inline(" "),
        KUNDEN.NAME, DSL.inline(" "),
        KUNDEN.ZS_NUMMER.cast(String::class.java), DSL.inline(" "),
        KUNDEN.ADRESSE, DSL.inline(" "),
        KUNDEN.STATUS
    )

	// Custom Query using Postgres specific word_similarity function
	fun getAllClientsFittingSearchterm(searchTerm: String): List<KundeViewModel> {
		// wordSimilarity is the pg_trgm function word_similarity(), reverse-engineered
		// by the code generator into a typed Kotlin function. No string SQL anywhere.
		val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHTEXT)
		return dsl.selectFrom(KUNDEN)
			.where(similarity.ge(clientSearchProperties.minimumSimilarity.toFloat()))
			.orderBy(similarity.desc())
			.limit(clientSearchProperties.maxAmountOfSearchResults)
			.fetch()
			.map { kundenRecord -> kundenRecord.toViewModel() }
	}

	fun getClients(
		sortData: SortData = SortData(),
		searchTerm: String?
	): List<KundeViewModel> {

		val sortField = when (sortData.sortDirection) {
			SortDirection.ASC -> sortData.sortColumn.field.asc()
			SortDirection.DESC -> sortData.sortColumn.field.desc()
		}

		if (searchTerm.isNullOrEmpty()) {
			return dsl.selectFrom(KUNDEN).orderBy(sortField).fetch().map { it.toViewModel() }
		} else {
			val similarity = wordSimilarity(DSL.`val`(searchTerm), KUNDEN_SUCHTEXT)
			return dsl.selectFrom(KUNDEN)
				.where(similarity.ge(clientSearchProperties.minimumSimilarity.toFloat()))
				.orderBy(similarity.desc(), sortField)
				.limit(clientSearchProperties.maxAmountOfSearchResults)
				.fetch()
				.map { kundenRecord -> kundenRecord.toViewModel() }

		}
	}

    // update() writes all columns of the POJO into the row with the same primary key
    // (UPDATE kunden SET ... WHERE id = ?). If that id does not exist, jOOQ silently updates
    // 0 rows, hence the explicit existence check.
    fun editExistingClient(kunde: KundeViewModel): KundeViewModel {
        val kundePojo = Kunden(
            id = UUID.fromString(kunde.id),
            name = kunde.name,
            kurzname = kunde.kurzname,
            zsNummer = kunde.zsNummer,
            adresse = kunde.adresse,
            status = kunde.status,
            besondersSchuetzenswert = kunde.istBesondersSchuetzenswert,
        )
        kundenDao.update(kundePojo)
        return kunde
    }
    //endregion
}

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
	val columnNames: EnumEntries<KundenSortColumn> = KundenSortColumn.entries,
	val sortColumn: KundenSortColumn = KundenSortColumn.KURZNAME,
	val sortDirection: SortDirection = SortDirection.ASC
)
//endregion

//region Mappers
// jOOQ's KotlinGenerator types every column as nullable, because any SQL expression can in
// principle yield NULL. All columns of kunden and verpflichtungen are declared NOT NULL in
// the schema (see src/main/resources/db/migration), so these non-null assertions are backed
// by the database itself. DAO methods return generated POJOs, DSL queries return Records —
// hence one mapper per generated type.


//Kunden is DAO type
fun Kunden.toViewModel(): KundeViewModel = KundeViewModel(
    id = this.id.toString(),
    kurzname = this.kurzname!!,
    name = this.name!!,
    zsNummer = this.zsNummer!!,
    adresse = this.adresse!!,
    status = this.status!!,
    istBesondersSchuetzenswert = this.besondersSchuetzenswert!!
)

fun Kunden.toDTO(): KundeDTO = KundeDTO(
	id = this.id,

)
//KundenRecord ist DSLContext type
fun KundenRecord.toViewModel(): KundeViewModel = KundeViewModel(
    id = id.toString(),
    kurzname = kurzname!!,
    name = name!!,
    zsNummer = zsNummer!!,
    adresse = adresse!!,
    status = status!!,
    istBesondersSchuetzenswert = besondersSchuetzenswert!!
)
//endregion
