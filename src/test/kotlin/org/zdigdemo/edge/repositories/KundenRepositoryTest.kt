package org.zdigdemo.edge.repositories

import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.zdigdemo.jooq.tables.records.KundenRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Exercises the real PostgreSQL queries, including pg_trgm search and keyset pagination,
 * against the seeded development database.
 */
@SpringBootTest
class KundenRepositoryTest {

	@Autowired
	lateinit var kundenRepository: KundenRepository

	@Test
	fun `unfiltered pagination returns the reported count without duplicates`() {
		val sortData = SortData()
		val firstPage = kundenRepository.getClients(sortData = sortData, searchTerm = "")

		val loadedClients = loadAllPages(firstPage, sortData, searchTerm = "")

		assertReportedCountAndUniqueIds(firstPage.amountSearchResult, loadedClients)
	}

	@Test
	fun `search pagination returns the reported count without duplicates`() {
		val sortData = SortData()
		val searchTerm = "Aktiv"
		val firstPage = kundenRepository.getClients(sortData = sortData, searchTerm = searchTerm)
		assertTrue(
			firstPage.amountSearchResult > firstPage.data.size,
			"The seed data must provide more than one search-result page for this regression test"
		)

		val loadedClients = loadAllPages(firstPage, sortData, searchTerm)

		assertReportedCountAndUniqueIds(firstPage.amountSearchResult, loadedClients)
	}

	private fun loadAllPages(
		firstPage: ClientPage,
		sortData: SortData,
		searchTerm: String
	): List<KundenRecord> {
		val loadedClients = firstPage.data.toMutableList()
		var lastSeenId = loadedClients.lastOrNull()?.id ?: return loadedClients

		repeat(200) {
			val nextPage = kundenRepository.getNextClientPage(
				lastSeenId = lastSeenId,
				sortData = sortData,
				searchTerm = searchTerm
			)
			if (nextPage.isEmpty()) return loadedClients

			loadedClients += nextPage
			lastSeenId = requireNotNull(nextPage.last().id)
		}

		fail("Pagination did not terminate after 200 additional pages")
	}

	private fun assertReportedCountAndUniqueIds(expectedCount: Int, loadedClients: List<KundenRecord>) {
		assertEquals(
			loadedClients.size,
			loadedClients.map { it.id }.toSet().size,
			"Pagination returned the same client more than once"
		)
		assertEquals(
			expectedCount,
			loadedClients.size,
			"The reported result count must equal the number of clients reachable through pagination"
		)
	}
}
