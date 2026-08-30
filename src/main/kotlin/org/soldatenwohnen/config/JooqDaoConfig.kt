package org.soldatenwohnen.config

import org.jooq.Configuration
import org.soldatenwohnen.jooq.tables.daos.KundenDao
import org.soldatenwohnen.jooq.tables.daos.VerpflichtungenDao
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration as SpringConfiguration

/**
 * Registers the jOOQ-generated DAOs as Spring beans.
 *
 * The injected [Configuration] is the one from Spring Boot's JooqAutoConfiguration —
 * the same instance that backs the [org.jooq.DSLContext] bean. Because it resolves
 * JDBC connections through Spring's TransactionAwareDataSourceProxy, every DAO call
 * automatically joins the surrounding @Transactional scope, exactly like DSLContext
 * queries do.
 */
@SpringConfiguration
class JooqDaoConfig {

	@Bean
	fun kundenDao(configuration: Configuration): KundenDao = KundenDao(configuration)

	@Bean
	fun verpflichtungenDao(configuration: Configuration): VerpflichtungenDao =
		VerpflichtungenDao(configuration)
}
