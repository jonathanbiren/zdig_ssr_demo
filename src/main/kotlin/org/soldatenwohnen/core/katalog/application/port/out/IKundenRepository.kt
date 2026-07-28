package org.soldatenwohnen.core.katalog.application.port.out

import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.soldatenwohnen.core.katalog.domain.model.KundeMitVerpflichtungen
import java.util.UUID

interface IKundenRepository {

	fun getAllClients(): List<Kunde>

	fun getClient(id: UUID): Kunde?

	fun deleteClient(id: UUID): Unit

	fun getKundeMitVerpflichtungen(id: UUID): KundeMitVerpflichtungen?

	fun getAllClientsFittingSearchterm(searchTerm: String): List<Kunde>
}