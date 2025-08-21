package org.deblock.flights.adapter.inbound.controller

import jakarta.validation.Valid
import org.deblock.flights.adapter.inbound.dto.FlightRequestDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO
import org.deblock.flights.domain.port.FlightsServiceUseCase
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flights")
@Validated
class FlightsController(
    private val flightSearchService: FlightsServiceUseCase,
) {
    @PostMapping
    fun searchFlights(
        @Valid @RequestBody flightRequestDTO: FlightRequestDTO,
    ): FlightResponseDTO {
        return flightSearchService.searchFlights(flightRequestDTO)
    }
}
