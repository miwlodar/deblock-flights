package org.deblock.flights.adapter.outbound.client.toughjet

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "toughjet")
data class ToughJetProperties(
    val url: String,
)
