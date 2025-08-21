package org.deblock.flights.adapter.inbound.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    @field:Min(value = 1, message = "There must be at least one passenger")
    @field:Max(value = 4, message = "There may be maximum 4 passengers")
    val numberOfPassengers: Int,
) {
    init {
        isDestinationDifferentThanOrigin()
        isDepartureBeforeReturn()
        areIATACodesValid()
    }

    private fun isDestinationDifferentThanOrigin() {
        require(origin != destination) { "Destination must be different than the origin" }
    }

    private fun isDepartureBeforeReturn() {
        require(departureDate.isEqual(returnDate) || departureDate.isBefore(returnDate)) {
            "Return date cannot be before departure date"
        }
    }

    private fun areIATACodesValid() {
        require(isIATACodeValid(origin)) { "IATA code for origin is invalid" }
        require(isIATACodeValid(destination)) { "IATA code for destination is invalid" }
    }

    private fun isIATACodeValid(iataCode: String): Boolean {
        val iataCodePattern: Pattern = Pattern.compile("^[A-Z]{3}$")
        return iataCodePattern.matcher(iataCode).matches()
    }
}
