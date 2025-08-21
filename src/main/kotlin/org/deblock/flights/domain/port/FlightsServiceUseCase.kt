package org.deblock.flights.domain.port

import org.deblock.flights.adapter.inbound.dto.FlightRequestDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO


interface FlightsServiceUseCase {
    fun searchFlights(request: FlightRequestDTO): FlightResponseDTO
}