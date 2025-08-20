package org.deblock.flights.domain.model

import java.time.LocalDate

data class FlightRequest(
    val origin: String,
    val destination: String,
    val departureDate: LocalDate,
    val returnDate: LocalDate,
    val numberOfPassengers: Int,
)
