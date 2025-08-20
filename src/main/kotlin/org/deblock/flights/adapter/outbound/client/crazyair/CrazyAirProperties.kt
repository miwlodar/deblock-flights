package org.deblock.flights.adapter.outbound.client.crazyair

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "crazyair")
data class CrazyAirProperties(
    val url: String,
)
