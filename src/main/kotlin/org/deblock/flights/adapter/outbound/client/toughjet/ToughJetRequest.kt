package org.deblock.flights.adapter.outbound.client.toughjet

import java.time.LocalDate

data class ToughJetRequest(
    val from: String,
    val to: String,
    val outboundDate: LocalDate,
    val inboundDate: LocalDate,
    val numberOfAdults: Int,
)
