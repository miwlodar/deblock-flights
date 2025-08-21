package org.deblock.flights.adapter.outbound.client.crazyair

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.format.DateTimeFormatter

@Service
class CrazyAirClient(
    private val restTemplate: RestTemplate,
    private val properties: CrazyAirProperties
) {
    fun searchFlights(request: CrazyAirRequest): List<CrazyAirFlight> =
        buildUri(request).let { uri ->
            restTemplate.getForEntity<Array<CrazyAirFlight>>(uri)
                .body
                ?.toList()
                ?: emptyList()
        }

    private fun buildUri(request: CrazyAirRequest): URI =
        UriComponentsBuilder.fromHttpUrl(properties.url)
            .apply {
                queryParam("origin", request.origin)
                queryParam("destination", request.destination)
                queryParam("departureDate", request.departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                queryParam("returnDate", request.returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                queryParam("passengerCount", request.passengerCount)
            }
            .build()
            .toUri()
}
