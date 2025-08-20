package org.deblock.flights.domain.service

import org.deblock.flights.domain.model.Flight
import org.deblock.flights.domain.model.FlightRequest


interface FlightsServiceUseCase {
    fun searchFlights(request: FlightRequest): List<Flight>
}