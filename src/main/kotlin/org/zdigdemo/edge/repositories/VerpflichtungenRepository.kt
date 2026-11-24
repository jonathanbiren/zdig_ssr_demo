package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.zdigdemo.jooq.tables.records.VerpflichtungenRecord
import org.zdigdemo.jooq.tables.references.VERPFLICHTUNGEN
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.zdigdemo.jooq.tables.references.KUNDEN
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
) {
    fun getVerpflichtungen(
        sortierInformation: VerpflichtungSortData = VerpflichtungSortData(),
        sucheingabe: String? = null,
    ): List<org.zdigdemo.edge.controller.VerpflichtungViewModel2> {
        val sortField = when (sortierInformation.sortDirection) {
            SortDirection.ASC -> sortierInformation.sortColumn.field.asc()
            SortDirection.DESC -> sortierInformation.sortColumn.field.desc()
        }

        val verpflichtungenSuchtext: Field<String?> = DSL.concat(
            VERPFLICHTUNGEN.BEZEICHNUNG,
            DSL.inline(" "),
            VERPFLICHTUNGEN.BETRAG_CENT.cast(String::class.java),
            DSL.inline(" "),
            KUNDEN.NAME
        )

        val query = dsl.select(
            VERPFLICHTUNGEN.ID,
            VERPFLICHTUNGEN.KUNDE_ID,
            KUNDEN.NAME,
            VERPFLICHTUNGEN.BEZEICHNUNG,
            VERPFLICHTUNGEN.BETRAG_CENT,
        )
            .from(VERPFLICHTUNGEN)
            .join(KUNDEN).on(KUNDEN.ID.eq(VERPFLICHTUNGEN.KUNDE_ID))

        val filteredQuery = if (sucheingabe.isNullOrBlank()) {
            query
        } else {
            query.where(verpflichtungenSuchtext.containsIgnoreCase(sucheingabe))
        }

        return filteredQuery
            .orderBy(sortField)
            .fetch()
            .map {
                org.zdigdemo.edge.controller.VerpflichtungViewModel2(
                    id = it.get(VERPFLICHTUNGEN.ID)!!.toString(),
                    kundeId = it.get(VERPFLICHTUNGEN.KUNDE_ID)!!.toString(),
                    kundeName = it.get(KUNDEN.NAME)!!,
                    verpflichtungName = it.get(VERPFLICHTUNGEN.BEZEICHNUNG)!!,
                    verpflichtungBetrag = it.get(VERPFLICHTUNGEN.BETRAG_CENT)!!.toString(),
                    personId = UUID.randomUUID().toString(),
                    personName = "Test"
                )
            }
    }
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
data class VerpflichtungSortData(
    val columnNames: EnumEntries<VerpflichtungSortColumn> = VerpflichtungSortColumn.entries,
    val sortColumn: VerpflichtungSortColumn = VerpflichtungSortColumn.BEZEICHNUNG,
    val sortDirection: SortDirection = SortDirection.ASC
)

enum class VerpflichtungSortColumn(
    val requestName: String,
    val viewName: String,
    val field: Field<*>
) {
    BEZEICHNUNG("bezeichnung", "Bezeichnung", VERPFLICHTUNGEN.BEZEICHNUNG),
    KUNDENNAME("kundenname", "Kundenname", KUNDEN.NAME),
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
