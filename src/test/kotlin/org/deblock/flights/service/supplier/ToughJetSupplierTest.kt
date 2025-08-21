package org.deblock.flights.service.supplier

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetClient
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetFlight
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetRequest
import org.deblock.flights.adapter.outbound.supplier.ToughJetSupplier
import org.deblock.flights.domain.model.FlightRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

internal class ToughJetSupplierTest {
    private val toughJetClient: ToughJetClient = mock()

    private val toughJetSupplier = ToughJetSupplier(toughJetClient)

    @Test
    fun `searchFlights should return a list of Flight`() =
        runTest {
            // Given
            val request =
                FlightRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = LocalDate.parse("2022-01-01"),
                    returnDate = LocalDate.parse("2022-01-10"),
                    numberOfPassengers = 2,
                )

            val toughJetSearchRequest =
                ToughJetRequest(
                    from = request.origin,
                    to = request.destination,
                    outboundDate = request.departureDate,
                    inboundDate = request.returnDate,
                    numberOfAdults = request.numberOfPassengers,
                )

            val toughJetFlights =
                listOf(
                    ToughJetFlight(
                        carrier = "ToughJet",
                        basePrice = BigDecimal(300),
                        tax = BigDecimal(50),
                        discount = BigDecimal(10),
                        departureAirportName = "LHR",
                        arrivalAirportName = "AMS",
                        outboundDateTime = Instant.parse("2022-01-01T10:00:00Z"),
                        inboundDateTime = Instant.parse("2022-01-01T12:00:00Z"),
                    ),
                    ToughJetFlight(
                        carrier = "ToughJet",
                        basePrice = BigDecimal(400),
                        tax = BigDecimal(50),
                        discount = BigDecimal(10),
                        departureAirportName = "LHR",
                        arrivalAirportName = "AMS",
                        outboundDateTime = Instant.parse("2022-01-01T14:00:00Z"),
                        inboundDateTime = Instant.parse("2022-01-01T16:00:00Z"),
                    ),
                )

            whenever(toughJetClient.searchFlights(toughJetSearchRequest)).thenReturn(toughJetFlights)

            // When
            val result = toughJetSupplier.searchFlights(request)

            // Then
            assertThat(result).hasSize(2)

            // Assertions for the first flight
            assertThat(result[0].airline).isEqualTo("ToughJet")
            assertThat(result[0].supplier).isEqualTo(ToughJetSupplier.TOUGH_JET_SUPPLIER)
            assertThat(result[0].fare).isEqualTo(BigDecimal("315.0"))
            assertThat(result[0].departureAirportCode).isEqualTo("LHR")
            assertThat(result[0].destinationAirportCode).isEqualTo("AMS")
            assertThat(result[0].departureDate).isEqualTo(Instant.parse("2022-01-01T10:00:00Z"))
            assertThat(result[0].arrivalDate).isEqualTo(Instant.parse("2022-01-01T12:00:00Z"))

            // Assertions for the second flight
            assertThat(result[1].airline).isEqualTo("ToughJet")
            assertThat(result[1].supplier).isEqualTo(ToughJetSupplier.TOUGH_JET_SUPPLIER)
            assertThat(result[1].fare).isEqualTo(BigDecimal("405.0"))
            assertThat(result[1].departureAirportCode).isEqualTo("LHR")
            assertThat(result[1].destinationAirportCode).isEqualTo("AMS")
            assertThat(result[1].departureDate).isEqualTo(Instant.parse("2022-01-01T14:00:00Z"))
            assertThat(result[1].arrivalDate).isEqualTo(Instant.parse("2022-01-01T16:00:00Z"))

            verify(toughJetClient, times(1)).searchFlights(toughJetSearchRequest)
        }

    @Test
    fun `searchFlights should return an empty list when ToughJetClient returns no flights`() =
        runTest {
            // Given
            val request =
                FlightRequest(
                    origin = "LHR",
                    destination = "AMS",
                    departureDate = LocalDate.parse("2022-01-01"),
                    returnDate = LocalDate.parse("2022-01-10"),
                    numberOfPassengers = 2,
                )

            val toughJetSearchRequest =
                ToughJetRequest(
                    from = request.origin,
                    to = request.destination,
                    outboundDate = request.departureDate,
                    inboundDate = request.returnDate,
                    numberOfAdults = request.numberOfPassengers,
                )

            `when`(toughJetClient.searchFlights(toughJetSearchRequest)).thenReturn(emptyList())

            // When
            val result = toughJetSupplier.searchFlights(request)

            // Then
            verify(toughJetClient, times(1)).searchFlights(toughJetSearchRequest)
            assertThat(result).isEmpty()
        }
}
