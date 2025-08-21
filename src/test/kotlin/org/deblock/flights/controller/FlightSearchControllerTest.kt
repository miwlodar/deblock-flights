package org.deblock.flights.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.deblock.flights.BaseTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class FlightSearchControllerTest : BaseTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val origin = "KRK"
    private val destination = "DPS"
    private val departureDate = "2025-09-10"
    private val returnDate = "2025-09-25"
    private val passengerCount = 3

    @Test
    fun `returns flights when only one supplier has results`() {
        stubCrazyAir()
        stubEmptyResponse("/toughjet/search")

        performFlightSearch()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flights").isArray)
            .andExpect(jsonPath("$.flights", hasSize<Any>(2)))
            .andExpect(jsonPath("$.flights[0].supplier").value("CrazyAir"))
            .andExpect(jsonPath("$.flights[0].fare").value(BigDecimal("350.0")))
            .andExpect(jsonPath("$.flights[1].supplier").value("CrazyAir"))
            .andExpect(jsonPath("$.flights[1].fare").value(BigDecimal("500.0")))
    }

    @Test
    fun `returns flights from all suppliers sorted by fare`() {
        stubCrazyAir()
        stubToughJet()

        performFlightSearch()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flights").isArray)
            .andExpect(jsonPath("$.flights", hasSize<Any>(3)))
            .andExpect(jsonPath("$.flights[0].fare").value(BigDecimal("350.0")))
            .andExpect(jsonPath("$.flights[1].fare").value(BigDecimal("360.0")))
            .andExpect(jsonPath("$.flights[2].fare").value(BigDecimal("500.0")))
    }

    @Test
    fun `returns empty list when no suppliers have flights`() {
        stubEmptyResponse("/crazyair/search")
        stubEmptyResponse("/toughjet/search")

        performFlightSearch()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flights").isEmpty)
    }

    private fun performFlightSearch() = mockMvc.perform(
        post("/api/flights")
            .content(
                """
                {
                    "origin": "$origin",
                    "destination": "$destination",
                    "departureDate": "$departureDate",
                    "returnDate": "$returnDate",
                    "numberOfPassengers": $passengerCount
                }
                """.trimIndent()
            )
            .contentType("application/json")
    )

    private fun stubCrazyAir(delayInMillis: Int = 0) {
        wireMockServer.stubFor(
            get(WireMock.urlPathEqualTo("/crazyair/search"))
                .withQueryParam("origin", equalTo(origin))
                .withQueryParam("destination", equalTo(destination))
                .withQueryParam("departureDate", equalTo(departureDate))
                .withQueryParam("returnDate", equalTo(returnDate))
                .withQueryParam("passengerCount", equalTo(passengerCount.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayInMillis)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
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
                        )
                )
        )
    }

    private fun stubToughJet(delayInMillis: Int = 0) {
        wireMockServer.stubFor(
            get(WireMock.urlPathEqualTo("/toughjet/search"))
                .withQueryParam("from", equalTo(origin))
                .withQueryParam("to", equalTo(destination))
                .withQueryParam("outboundDate", equalTo(departureDate))
                .withQueryParam("inboundDate", equalTo(returnDate))
                .withQueryParam("numberOfAdults", equalTo(passengerCount.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayInMillis)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                        [
                            {
                                "carrier": "Qatar Airways",
                                "basePrice": 350.0,
                                "tax": 10.0,
                                "discount": 0,
                                "departureAirportName": "$origin",
                                "arrivalAirportName": "$destination",
                                "outboundDateTime": "2025-09-10T07:00:00Z",
                                "inboundDateTime": "2025-09-10T19:30:00Z"
                            }
                        ]
                        """.trimIndent()
                        )
                )
        )
    }

    private fun stubEmptyResponse(supplierUrl: String) {
        wireMockServer.stubFor(
            get(WireMock.urlPathEqualTo(supplierUrl))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                )
        )
    }
}
