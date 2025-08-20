package org.deblock.flights.adapter.inbound.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.deblock.flights.domain.model.FlightRequest

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.regex.Pattern

data class FlightRequestDTO(
    val origin: String,
    val destination: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val departureDate: LocalDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val returnDate: LocalDate,
    @field:Min(value = 1, message = "Number of passengers should be at least 1")
    @field:Max(value = 4, message = "Number of passengers cannot exceed 4")
    val numberOfPassengers: Int,
) {
    init {
        validateOriginAndDestination()
        validateFlightDates()
    }

    private fun validateOriginAndDestination() {
        validateIATACodes()
        validateDifferentOriginAndDestination()
    }

    private fun validateIATACodes() {
        require(isValidIATACode(origin)) { "Invalid IATA code for origin" }
        require(isValidIATACode(destination)) { "Invalid IATA code for destination" }
    }

    private fun isValidIATACode(iataCode: String): Boolean {
        val iataCodePattern: Pattern = Pattern.compile("^[A-Z]{3}$")
        return iataCodePattern.matcher(iataCode).matches()
    }

    private fun validateDifferentOriginAndDestination() {
        require(origin != destination) { "Origin and destination must be different" }
    }

    private fun validateFlightDates() {
        require(departureDate.isEqual(returnDate) || departureDate.isBefore(returnDate)) {
            "Departure date must be on or before return date"
        }
    }
}

fun FlightRequestDTO.toSearchRequest(): FlightRequest {
    return FlightRequest(
        origin = this.origin,
        destination = this.destination,
        departureDate = this.departureDate,
        returnDate = this.returnDate,
        numberOfPassengers = this.numberOfPassengers,
    )
}
