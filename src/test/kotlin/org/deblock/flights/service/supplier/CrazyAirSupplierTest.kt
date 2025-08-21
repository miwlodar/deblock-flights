package org.deblock.flights.service.supplier

import kotlinx.coroutines.test.runTest
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirClient
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirFlight
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirRequest
import org.deblock.flights.adapter.outbound.supplier.CrazyAirSupplier
import org.deblock.flights.domain.model.FlightRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

internal class CrazyAirSupplierTest {
    private val crazyAirClient: CrazyAirClient = mock()

    private val crazyAirSupplier = CrazyAirSupplier(crazyAirClient)

    @Test
    fun `searchFlights should return a list of Flight`() =
        runTest {
            // Given
            val flightSearchRequest =
                FlightRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = LocalDate.parse("2022-01-01"),
                    returnDate = LocalDate.parse("2022-01-10"),
                    numberOfPassengers = 2,
                )

            val crazyAirSearchRequest =
                CrazyAirRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = flightSearchRequest.departureDate,
                    returnDate = flightSearchRequest.returnDate,
                    passengerCount = flightSearchRequest.numberOfPassengers,
                )

            val crazyAirFlights =
                listOf(
                    CrazyAirFlight(
                        airline = "British Airways",
                        price = BigDecimal(350),
                        cabinclass = "E",
                        departureAirportCode = "LHR",
                        destinationAirportCode = "AMS",
                        departureDate = LocalDateTime.parse("2022-01-01T10:00:00"),
                        arrivalDate = LocalDateTime.parse("2022-01-01T12:00:00"),
                    ),
                    CrazyAirFlight(
                        airline = "Lufthansa",
                        price = BigDecimal(400),
                        cabinclass = "B",
                        departureAirportCode = "LHR",
                        destinationAirportCode = "AMS",
                        departureDate = LocalDateTime.parse("2022-01-01T14:00:00"),
                        arrivalDate = LocalDateTime.parse("2022-01-01T16:00:00"),
                    ),
                )

            whenever(crazyAirClient.searchFlights(crazyAirSearchRequest)).thenReturn(crazyAirFlights)

            // When
            val result = crazyAirSupplier.searchFlights(flightSearchRequest)

            // Then
            assertEquals(2, result.size)

            // Assertions for the first flight
            assertEquals("British Airways", result[0].airline)
            assertEquals(BigDecimal(350), result[0].fare)
            assertEquals("LHR", result[0].departureAirportCode)
            assertEquals("AMS", result[0].destinationAirportCode)
            assertEquals(Instant.parse("2022-01-01T10:00:00Z"), result[0].departureDate)
            assertEquals(Instant.parse("2022-01-01T12:00:00Z"), result[0].arrivalDate)

            // Assertions for the second flight
            assertEquals("Lufthansa", result[1].airline)
            assertEquals(BigDecimal(400), result[1].fare)
            assertEquals("LHR", result[1].departureAirportCode)
            assertEquals("AMS", result[1].destinationAirportCode)
            assertEquals(Instant.parse("2022-01-01T14:00:00Z"), result[1].departureDate)
            assertEquals(Instant.parse("2022-01-01T16:00:00Z"), result[1].arrivalDate)

            verify(crazyAirClient, times(1)).searchFlights(crazyAirSearchRequest)
        }

    @Test
    fun `searchFlights should return an empty list when no flights are available`() =
        runTest {
            // Given
            val flightSearchRequest =
                FlightRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = LocalDate.parse("2022-01-01"),
                    returnDate = LocalDate.parse("2022-01-10"),
                    numberOfPassengers = 2,
                )

            val crazyAirSearchRequest =
                CrazyAirRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = flightSearchRequest.departureDate,
                    returnDate = flightSearchRequest.returnDate,
                    passengerCount = flightSearchRequest.numberOfPassengers,
                )

            whenever(crazyAirClient.searchFlights(crazyAirSearchRequest)).thenReturn(emptyList())

            // When
            val result = crazyAirSupplier.searchFlights(flightSearchRequest)

            // Then
            assertTrue(result.isEmpty())
        }
}
