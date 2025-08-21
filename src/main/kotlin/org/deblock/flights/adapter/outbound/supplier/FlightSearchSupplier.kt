package org.deblock.flights.adapter.outbound.supplier

import org.deblock.flights.domain.model.Flight
import org.deblock.flights.domain.model.FlightRequest


interface FlightSearchSupplier {
    suspend fun searchFlights(request: FlightRequest): List<Flight>
}
