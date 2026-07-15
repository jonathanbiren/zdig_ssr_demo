package org.soldatenwohnen.adapter.out

import org.soldatenwohnen.core.katalog.domain.model.Verpflichtung
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Component
class VerpflichtungRepository(
	val jdbcVerpflichtungRepository: JdbcVerpflichtungRepository,
) {

	fun findByKundeId(kundeId: UUID): List<Verpflichtung> =
		jdbcVerpflichtungRepository.findByKundeId(kundeId).map { it.toDomain() }

	fun findById(id: UUID): Verpflichtung? =
		jdbcVerpflichtungRepository.findByIdOrNull(id)?.toDomain()

	fun save(verpflichtung: Verpflichtung): Verpflichtung =
		jdbcVerpflichtungRepository.save(verpflichtung.toRow()).toDomain()

	fun deleteById(id: UUID) =
		jdbcVerpflichtungRepository.deleteById(id)
}

@Repository
interface JdbcVerpflichtungRepository : CrudRepository<VerpflichtungRow, UUID> {

	fun findByKundeId(kundeId: UUID): List<VerpflichtungRow>
}

@Table("verpflichtungen")
data class VerpflichtungRow(
	@Id
	val id: UUID,
	val kundeId: UUID,
	val bezeichnung: String,
	val betragCent: Long,
	val erstelltAm: OffsetDateTime = OffsetDateTime.now()
) {
	fun toDomain(): Verpflichtung = Verpflichtung(
		id = this.id,
		kundeId = this.kundeId,
		bezeichnung = this.bezeichnung,
		betragCent = this.betragCent,
		erstelltAm = this.erstelltAm
	)
}

fun Verpflichtung.toRow(): VerpflichtungRow = VerpflichtungRow(
	id = this.id,
	kundeId = this.kundeId,
	bezeichnung = this.bezeichnung,
	betragCent = this.betragCent,
	erstelltAm = this.erstelltAm
)

