package org.deblock.flights.adapter.outbound.client.toughjet

import java.math.BigDecimal
import java.time.Instant

data class ToughJetFlight(
    val carrier: String,
    val basePrice: BigDecimal,
    val tax: BigDecimal,
    val discount: BigDecimal,
    val departureAirportName: String,
    val arrivalAirportName: String,
    val outboundDateTime: Instant,
    val inboundDateTime: Instant,
)
