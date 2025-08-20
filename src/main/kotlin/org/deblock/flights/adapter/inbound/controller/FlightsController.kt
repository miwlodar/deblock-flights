package org.deblock.flights.adapter.inbound.controller

import jakarta.validation.Valid
import org.deblock.flights.adapter.inbound.dto.FlightRequestDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO
import org.deblock.flights.adapter.inbound.dto.toSearchRequest
import org.deblock.flights.adapter.inbound.mapper.FlightsMapper
import org.deblock.flights.domain.service.FlightsServiceUseCase
//import org.deblock.flights.domain.service.FlightSearchServiceUseCase


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
    @PostMapping("/search")
    fun search(
        @Valid @RequestBody flightRequestDTO: FlightRequestDTO,
    ): FlightResponseDTO {
        val searchResult = flightSearchService.searchFlights(flightRequestDTO.toSearchRequest())
        return FlightsMapper.map(searchResult)
    }
}
