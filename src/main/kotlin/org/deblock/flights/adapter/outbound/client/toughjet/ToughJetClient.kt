package org.deblock.flights.adapter.outbound.client.toughjet

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class ToughJetClient(
    private val restTemplate: RestTemplate,
    private val properties: ToughJetProperties
) {
    fun searchFlights(request: ToughJetRequest): List<ToughJetFlight> =
        buildUri(request).let { uri ->
            restTemplate.getForEntity<Array<ToughJetFlight>>(uri)
                .body
                ?.toList()
                ?: emptyList()
        }

    private fun buildUri(request: ToughJetRequest): URI =
        UriComponentsBuilder.fromHttpUrl(properties.url)
            .apply {
                queryParam("from", request.from)
                queryParam("to", request.to)
                queryParam("outboundDate", request.outboundDate)
                queryParam("inboundDate", request.inboundDate)
                queryParam("numberOfAdults", request.numberOfAdults)
            }
            .build()
            .toUri()
}
