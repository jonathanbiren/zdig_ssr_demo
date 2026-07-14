package org.soldatenwohnen.adapter.out

import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Component
class KundenRepository(jdbcRepository: SpringKundenRepository) {
	public fun getAllClients(): List<Kunde> {

	}
}

@Repository
interface SpringKundenRepository : CrudRepository<KundenRow, UUID> {}

@Table(name = "kunden")
data class KundenRow(
	@Id
	var id: UUID,
	var kurzname: String,
	var name: String,
	var zsNummer: Long,
	var adresse: String,
	var status: String,
	var besondersSchuetzenswert: Boolean,
	var erstelltAm: OffsetDateTime = OffsetDateTime.now()
) {
	fun toDomain(): Kunde = Kunde(
		id = this.id,
		kurzname = this.kurzname,
		name = this.name,
		zsNummer = this.zsNummer,
		adresse = this.adresse,
		status = this.status,
		besondersSchuetzenswert = this.besondersSchuetzenswert
	)
}







