package org.deblock.flights.adapter.inbound.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class FlightResponseDTO(val flights: List<FlightDTO>)

data class FlightDTO(
    val airline: String,
    val supplier: String,
    val fare: BigDecimal,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime,
)
