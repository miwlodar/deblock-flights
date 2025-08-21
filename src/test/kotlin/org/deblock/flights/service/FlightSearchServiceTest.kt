package org.deblock.flights.service

import kotlinx.coroutines.test.runTest
import org.deblock.flights.adapter.inbound.dto.FlightRequestDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO
import org.deblock.flights.adapter.outbound.supplier.FlightSearchSupplier
import org.deblock.flights.application.service.FlightsService
import org.deblock.flights.domain.model.Flight
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class FlightSearchServiceTest {

    private val origin = "KRK"
    private val destination = "DPS"
    private val departureDate = LocalDate.parse("2025-09-10")
    private val returnDate = LocalDate.parse("2025-09-25")
    private val numberOfPassengers = 3

    @Test
    fun `searchFlights aggregates and sorts flights from multiple suppliers`() = runTest {
        val supplier1 = mockSupplier(
            listOf(flight("AirA", BigDecimal(200), "Supplier1"), flight("AirB", BigDecimal(150), "Supplier1"))
        )
        val supplier2 = mockSupplier(
            listOf(flight("AirC", BigDecimal(180), "Supplier2"), flight("AirD", BigDecimal(220), "Supplier2"))
        )
        val service = FlightsService(listOf(supplier1, supplier2))

        val result: FlightResponseDTO = service.searchFlights(searchRequestDTO())

        val flights = result.flights
        assertEquals(4, flights.size)
        assertEquals("AirB", flights[0].airline)
        assertEquals(BigDecimal(150), flights[0].fare)
        assertEquals("AirC", flights[1].airline)
        assertEquals(BigDecimal(180), flights[1].fare)
        assertEquals("AirA", flights[2].airline)
        assertEquals(BigDecimal(200), flights[2].fare)
        assertEquals("AirD", flights[3].airline)
        assertEquals(BigDecimal(220), flights[3].fare)
    }

    @Test
    fun `searchFlights returns flights from one supplier if another returns nothing`() = runTest {
        val supplier1 = mockSupplier(emptyList())
        val supplier2 = mockSupplier(listOf(flight("TestAir", BigDecimal(100))))

        val service = FlightsService(listOf(supplier1, supplier2))
        val result = service.searchFlights(searchRequestDTO())

        assertEquals(1, result.flights.size)
        assertEquals("TestAir", result.flights[0].airline)
        assertEquals(BigDecimal(100), result.flights[0].fare)
    }

    @Test
    fun `searchFlights returns empty list when all suppliers return nothing`() = runTest {
        val supplier1 = mockSupplier(emptyList())
        val supplier2 = mockSupplier(emptyList())

        val service = FlightsService(listOf(supplier1, supplier2))
        val result = service.searchFlights(searchRequestDTO())

        assertTrue(result.flights.isEmpty())
    }

    @Test
    fun `searchFlights returns flights from other suppliers when one throws an error`() = runTest {
        val supplier1 = mockSupplier(emptyList())
        whenever(supplier1.searchFlights(any())).thenThrow(RuntimeException())
        val supplier2 = mockSupplier(listOf(flight("TestAir", BigDecimal(100))))

        val service = FlightsService(listOf(supplier1, supplier2))
        val result = service.searchFlights(searchRequestDTO())

        assertEquals(1, result.flights.size)
        assertEquals("TestAir", result.flights[0].airline)
        assertEquals(BigDecimal(100), result.flights[0].fare)
    }

    @Test
    fun `searchFlights returns empty list when all suppliers throw errors`() = runTest {
        val supplier1 = mockSupplier(emptyList())
        whenever(supplier1.searchFlights(any())).thenThrow(RuntimeException())
        val supplier2 = mockSupplier(emptyList())
        whenever(supplier2.searchFlights(any())).thenThrow(RuntimeException())

        val service = FlightsService(listOf(supplier1, supplier2))
        val result = service.searchFlights(searchRequestDTO())

        assertTrue(result.flights.isEmpty())
    }

    private suspend fun mockSupplier(flights: List<Flight>): FlightSearchSupplier {
        val mockSupplier = mock<FlightSearchSupplier>()
        whenever(mockSupplier.searchFlights(any())).thenReturn(flights)
        return mockSupplier
    }

    private fun flight(
        airline: String,
        fare: BigDecimal,
        supplier: String = "MockSupplier",
    ): Flight {
        return Flight(
            airline = airline,
            supplier = supplier,
            fare = fare,
            departureAirportCode = origin,
            destinationAirportCode = destination,
            departureDate = Instant.parse("2025-09-10T08:00:00Z"),
            arrivalDate = Instant.parse("2025-09-10T20:00:00Z"),
        )
    }

    private fun searchRequestDTO(): FlightRequestDTO {
        return FlightRequestDTO(
            origin = origin,
            destination = destination,
            departureDate = departureDate,
            returnDate = returnDate,
            numberOfPassengers = numberOfPassengers,
        )
    }
}
