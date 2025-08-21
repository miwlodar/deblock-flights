package org.deblock.flights.service.client.toughjet

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.deblock.flights.BaseTest
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetClient
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetFlight
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ToughJetClientTest : BaseTest() {

    @Autowired
    private lateinit var toughJetClient: ToughJetClient

    private val from = "KRK"
    private val to = "DPS"
    private val outboundDate = LocalDate.parse("2025-09-10")
    private val inboundDate = LocalDate.parse("2025-09-25")
    private val numberOfAdults = 3

    private fun createSearchRequest() = ToughJetRequest(
        from = from,
        to = to,
        outboundDate = outboundDate,
        inboundDate = inboundDate,
        numberOfAdults = numberOfAdults
    )

    private fun stubToughJetResponse(flightsJson: String) {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/toughjet/search"))
                .withQueryParam("from", equalTo(from))
                .withQueryParam("to", equalTo(to))
                .withQueryParam("outboundDate", equalTo(outboundDate.toString()))
                .withQueryParam("inboundDate", equalTo(inboundDate.toString()))
                .withQueryParam("numberOfAdults", equalTo(numberOfAdults.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(flightsJson)
                )
        )
    }

    @Test
    fun `searchFlights returns flights from ToughJet`() {
        // Given
        val searchRequest = createSearchRequest()
        val flightsJson = """
            [
                {
                    "carrier": "ToughJet",
                    "basePrice": 300.0,
                    "tax": 50.0,
                    "discount": 10,
                    "departureAirportName": "$from",
                    "arrivalAirportName": "$to",
                    "outboundDateTime": "2025-09-10T08:00:00Z",
                    "inboundDateTime": "2025-09-25T20:00:00Z"
                }
            ]
        """.trimIndent()
        stubToughJetResponse(flightsJson)

        // When
        val result: List<ToughJetFlight> = toughJetClient.searchFlights(searchRequest)

        // Then
        assertThat(result).hasSize(1)
        val toughJetFlight = result[0]
        assertThat(toughJetFlight.carrier).isEqualTo("ToughJet")
        assertThat(toughJetFlight.basePrice).isEqualTo(BigDecimal("300.0"))
        assertThat(toughJetFlight.tax).isEqualTo(BigDecimal("50.0"))
        assertThat(toughJetFlight.discount).isEqualTo(BigDecimal(10))
        assertThat(toughJetFlight.departureAirportName).isEqualTo(from)
        assertThat(toughJetFlight.arrivalAirportName).isEqualTo(to)
        assertThat(toughJetFlight.outboundDateTime).isEqualTo(Instant.parse("2025-09-10T08:00:00Z"))
        assertThat(toughJetFlight.inboundDateTime).isEqualTo(Instant.parse("2025-09-25T20:00:00Z"))
    }

    @Test
    fun `searchFlights returns empty list when no flights are found`() {
        // Given
        val searchRequest = createSearchRequest()
        stubToughJetResponse("[]")

        // When
        val result: List<ToughJetFlight> = toughJetClient.searchFlights(searchRequest)

        // Then
        assertThat(result).isEmpty()
    }
}
