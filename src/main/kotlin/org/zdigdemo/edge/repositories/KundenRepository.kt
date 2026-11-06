package org.zdigdemo.edge.repositories

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.zdigdemo.config.ClientSearchProperties
import org.zdigdemo.jooq.routines.references.wordSimilarity
import org.zdigdemo.jooq.tables.daos.KundenDao
import org.zdigdemo.jooq.tables.pojos.Kunden
import org.zdigdemo.jooq.tables.records.KundenRecord
import org.zdigdemo.jooq.tables.references.KUNDEN
import java.util.*

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
}


//region Mappers
// jOOQ's KotlinGenerator types every column as nullable, because any SQL expression can in
// principle yield NULL. All columns of kunden and verpflichtungen are declared NOT NULL in
// the schema (see src/main/resources/db/migration), so these non-null assertions are backed
// by the database itself. DAO methods return generated POJOs, DSL queries return Records —
// hence one mapper per generated type.
data class KundeViewModel(
	val id: String,
	val kurzname: String,
	val name: String,
	val zsNummer: Long,
	val adresse: String,
	val status: String,
	val istBesondersSchuetzenswert: Boolean,
) {}

//Kunden is DAO type
fun Kunden.toViewModel(): KundeViewModel = KundeViewModel(
	id = id.toString(),
	kurzname = kurzname!!,
	name = name!!,
	zsNummer = zsNummer!!,
	adresse = adresse!!,
	status = status!!,
	istBesondersSchuetzenswert = besondersSchuetzenswert!!
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
