package org.deblock.flights.service.client.crazyair

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.deblock.flights.BaseTest
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirClient
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirFlight
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CrazyAirClientTest : BaseTest() {

    @Autowired
    private lateinit var crazyAirClient: CrazyAirClient

    private val origin = "KRK"
    private val destination = "DPS"
    private val departureDate = LocalDateTime.parse("2025-09-10T08:00:00", DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
    private val returnDate = LocalDateTime.parse("2025-09-25T20:00:00", DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
    private val passengerCount = 3

    private fun createSearchRequest() = CrazyAirRequest(
        origin = origin,
        destination = destination,
        departureDate = departureDate,
        returnDate = returnDate,
        passengerCount = passengerCount
    )

    private fun stubCrazyAirResponse(flightsJson: String) {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/crazyair/search"))
                .withQueryParam("origin", equalTo(origin))
                .withQueryParam("destination", equalTo(destination))
                .withQueryParam("departureDate", equalTo(departureDate.toString()))
                .withQueryParam("returnDate", equalTo(returnDate.toString()))
                .withQueryParam("passengerCount", equalTo(passengerCount.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(flightsJson)
                )
        )
    }

    @Test
    fun `searchFlights returns flights from CrazyAir`() {
        // Given
        val searchRequest = createSearchRequest()
        val flightsJson = """
            [
                {
                    "airline": "Emirates",
                    "price": 350.0,
                    "cabinclass": "E",
                    "departureAirportCode": "$origin",
                    "destinationAirportCode": "$destination",
                    "departureDate": "2025-09-10T08:00:00",
                    "arrivalDate": "2025-09-10T20:00:00"
                },
                {
                    "airline": "Singapore Airlines",
                    "price": 500.0,
                    "cabinclass": "B",
                    "departureAirportCode": "$origin",
                    "destinationAirportCode": "$destination",
                    "departureDate": "2025-09-10T10:00:00",
                    "arrivalDate": "2025-09-10T22:00:00"
                }
            ]
        """.trimIndent()
        stubCrazyAirResponse(flightsJson)

        // When
        val result: List<CrazyAirFlight> = crazyAirClient.searchFlights(searchRequest)

        // Then
        assertThat(result).hasSize(2)

        assertThat(result[0].airline).isEqualTo("Emirates")
        assertThat(result[0].price).isEqualTo(BigDecimal("350.0"))
        assertThat(result[0].cabinclass).isEqualTo("E")
        assertThat(result[0].departureAirportCode).isEqualTo(origin)
        assertThat(result[0].destinationAirportCode).isEqualTo(destination)
        assertThat(result[0].departureDate).isEqualTo(LocalDateTime.parse("2025-09-10T08:00:00"))
        assertThat(result[0].arrivalDate).isEqualTo(LocalDateTime.parse("2025-09-10T20:00:00"))

        assertThat(result[1].airline).isEqualTo("Singapore Airlines")
        assertThat(result[1].price).isEqualTo(BigDecimal("500.0"))
        assertThat(result[1].cabinclass).isEqualTo("B")
        assertThat(result[1].departureAirportCode).isEqualTo(origin)
        assertThat(result[1].destinationAirportCode).isEqualTo(destination)
        assertThat(result[1].departureDate).isEqualTo(LocalDateTime.parse("2025-09-10T10:00:00"))
        assertThat(result[1].arrivalDate).isEqualTo(LocalDateTime.parse("2025-09-10T22:00:00"))
    }

    @Test
    fun `searchFlights returns empty list when no flights are found`() {
        // Given
        val searchRequest = createSearchRequest()
        stubCrazyAirResponse("[]")

        // When
        val result: List<CrazyAirFlight> = crazyAirClient.searchFlights(searchRequest)

        // Then
        assertThat(result).isEmpty()
    }
}
