package org.deblock.flights.adapter.inbound.mapper


import org.deblock.flights.adapter.inbound.dto.FlightDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO
import org.deblock.flights.domain.model.Flight


import java.time.LocalDateTime
import java.time.ZoneOffset

object FlightsMapper {
    fun map(searchResult: List<Flight>): FlightResponseDTO {
        return FlightResponseDTO(
            searchResult.map { map(it) },
        )
    }

    private fun map(flight: Flight): FlightDTO {
        return FlightDTO(
            flight.airline,
            flight.supplier,
            flight.fare,
            flight.departureAirportCode,
            flight.destinationAirportCode,
            LocalDateTime.ofInstant(flight.departureDate, ZoneOffset.UTC),
            LocalDateTime.ofInstant(flight.arrivalDate, ZoneOffset.UTC),
        )
    }
}
