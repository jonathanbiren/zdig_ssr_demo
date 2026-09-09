package org.soldatenwohnen.config

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "search.kunden")
data class ClientSearchProperties(
	@field:DecimalMin("0.0")
	@field:DecimalMax("1.0")
	val minimumSimilarity: Double = 0.4,

	@field:Min(1)
	@field:Max(100)
	val maxAmountOfSearchResults: Int = 100
)
