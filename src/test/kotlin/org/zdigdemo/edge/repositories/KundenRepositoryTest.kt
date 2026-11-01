package org.zdigdemo.edge.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Runs against the real PostgreSQL database (started via docker compose), using the
 * seed data from V2__seed_kunden.sql and V5__seed_verpflichtungen.sql.
 */
//@SpringBootTest
//class KundenRepositoryTest {
//
//	@Autowired
//	lateinit var kundenRepository: KundenRepository
//
//	@Test
//	fun `trigram search ranks exact word match first`() {
//		val ergebnis = kundenRepository.getAllClientsFittingSearchterm("Müller")
//
//		assertTrue(ergebnis.isNotEmpty(), "Suche nach 'Müller' sollte Treffer liefern")
//		assertEquals("Müller GmbH", ergebnis.first().name)
//	}
//
//	@Test
//	fun `aggregate load returns kunde with all verpflichtungen`() {
//		// Seed data: 'Ankundigung Inc' has exactly two Verpflichtungen
//		val ankundigungIncId = UUID.fromString("0d4f5b72-6d5d-4a30-9c4d-1f1a0c2f1001")
//
//		val kunde = kundenRepository.getKundeMitVerpflichtungen(ankundigungIncId)
//
//		assertNotNull(kunde)
//		assertEquals("Ankundigung Inc", kunde.kunde.name)
//		assertEquals(2, kunde.verpflichtungen.size)
//		assertEquals(
//			setOf("Grundversorgung 2024", "Wartungsvertrag Netz"),
//			kunde.verpflichtungen.map { it.bezeichnung }.toSet()
//		)
//	}
//}
