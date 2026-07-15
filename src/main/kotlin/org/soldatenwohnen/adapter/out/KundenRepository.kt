package org.soldatenwohnen.adapter.out

import org.soldatenwohnen.config.ClientSearchProperties
import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Component
class KundenRepository(
	val clientRepository: JDBCKundenRepository,
	val clientSearchProperties: ClientSearchProperties,
) {

	fun getAllClients(): List<Kunde> {
		return clientRepository.findAll().map { it.toDomain() }
	}

	fun getClient(id: UUID): Kunde? {
		return clientRepository.findByIdOrNull(id)?.toDomain()
	}

	fun deleteClient(id: UUID) {
		clientRepository.deleteById(id)
	}

	fun getAllClientsFittingSearchterm(searchTerm: String): List<Kunde> {
		val listeGefilterterKunden: List<Kunde> = clientRepository
			.findAllBySearchTerm(
				searchTerm = searchTerm,
				minimumSimilarity = clientSearchProperties.minimumSimilarity,
				maxAmountOfSearchResults = clientSearchProperties.maxAmountOfSearchResults,
			)
			.map { it.toDomain() }
		return listeGefilterterKunden
	}
}


@Repository
interface JDBCKundenRepository : CrudRepository<KundeRow, UUID> {

	@Query(
		"""
		SELECT *
		FROM kunden
		WHERE word_similarity(:searchTerm, concat_ws(' ', kurzname, name, zs_nummer::varchar, adresse, status)) >=
		      :minimumSimilarity
		ORDER BY word_similarity(:searchTerm, concat_ws(' ', kurzname, name, zs_nummer::varchar, adresse, status)) DESC 
		LIMIT :maxSize
	"""
	)
	fun findAllBySearchTerm(
		@Param("searchTerm") searchTerm: String,
		@Param("minimumSimilarity") minimumSimilarity: Double,
		@Param("maxSize") maxAmountOfSearchResults: Int,
	): List<KundeRow>
}


@Table(name = "kunden")
data class KundeRow(
	@Id
	val id: UUID,
	val kurzname: String,
	val name: String,
	val zsNummer: Long,
	val adresse: String,
	val status: String,
	val besondersSchuetzenswert: Boolean,
	val erstelltAm: OffsetDateTime = OffsetDateTime.now()
) {
	fun toDomain(): Kunde = Kunde(
		id = this.id,
		kurzname = this.kurzname,
		name = this.name,
		zsNummer = this.zsNummer,
		adresse = this.adresse,
		status = this.status,
		istBesondersSchuetzenswert = this.besondersSchuetzenswert
	)
}







