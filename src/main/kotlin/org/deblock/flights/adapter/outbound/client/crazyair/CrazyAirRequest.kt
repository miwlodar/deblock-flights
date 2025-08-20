package org.deblock.flights.adapter.outbound.client.crazyair

import java.time.LocalDate

data class CrazyAirRequest(
    val origin: String,
    val destination: String,
    val departureDate: LocalDate,
    val returnDate: LocalDate,
    val passengerCount: Int,
)
