package org.deblock.flights.infrastructure.configuration

import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirProperties
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(value = [CrazyAirProperties::class, ToughJetProperties::class])
class FlightsConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate(SimpleClientHttpRequestFactory())
    }

}
