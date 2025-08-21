package org.deblock.flights.adapter.outbound.supplier


import org.deblock.flights.domain.model.Flight
import org.deblock.flights.domain.model.FlightRequest
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetClient
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetFlight
import org.deblock.flights.adapter.outbound.client.toughjet.ToughJetRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ToughJetSupplier(private val toughJetClient: ToughJetClient) : FlightSearchSupplier {
    override suspend fun searchFlights(request: FlightRequest): List<Flight> {
        val toughJetRequest =
            ToughJetRequest(
                from = request.origin,
                to = request.destination,
                outboundDate = request.departureDate,
                inboundDate = request.returnDate,
                numberOfAdults = request.numberOfPassengers,
            )

        val toughJetFlights = toughJetClient.searchFlights(toughJetRequest)

        return toughJetFlights.map { toughJetFlight ->
            mapToFlight(toughJetFlight)
        }
    }

    private fun mapToFlight(toughJetFlight: ToughJetFlight): Flight {
        return Flight(
            airline = toughJetFlight.carrier,
            supplier = TOUGH_JET_SUPPLIER,
            fare = calculateFare(toughJetFlight.basePrice, toughJetFlight.tax, toughJetFlight.discount),
            departureAirportCode = toughJetFlight.departureAirportName,
            destinationAirportCode = toughJetFlight.arrivalAirportName,
            departureDate = toughJetFlight.outboundDateTime,
            arrivalDate = toughJetFlight.inboundDateTime,
        )
    }

    private fun calculateFare(
        basePrice: BigDecimal,
        tax: BigDecimal,
        discount: BigDecimal,
    ): BigDecimal {
        val fullPrice = basePrice.add(tax)
        val discountAmount = fullPrice.multiply(discount.divide(BigDecimal(100)))
        return fullPrice - discountAmount
    }

    companion object {
        const val TOUGH_JET_SUPPLIER = "ToughJet"
    }
}
