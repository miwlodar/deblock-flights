package org.deblock.flights.adapter.outbound.supplier


import org.deblock.flights.domain.model.Flight
import org.deblock.flights.domain.model.FlightRequest
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirClient
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirFlight
import org.deblock.flights.adapter.outbound.client.crazyair.CrazyAirRequest
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.stream.Collectors

@Service
class CrazyAirSupplier(private val crazyAirClient: CrazyAirClient) : FlightSearchSupplier {
    override suspend fun searchFlights(request: FlightRequest): List<Flight> {
        val crazyAirSearchRequest = mapToCrazyAirSearchRequest(request)
        val crazyAirFlights = crazyAirClient.searchFlights(crazyAirSearchRequest)
        return mapToFlights(crazyAirFlights)
    }

    private fun mapToCrazyAirSearchRequest(request: FlightRequest): CrazyAirRequest {
        return CrazyAirRequest(
            origin = request.origin,
            destination = request.destination,
            departureDate = request.departureDate,
            returnDate = request.returnDate,
            passengerCount = request.numberOfPassengers,
        )
    }

    private fun mapToFlights(crazyAirFlights: List<CrazyAirFlight>): List<Flight> {
        return crazyAirFlights.stream()
            .map { crazyAirFlight ->
                Flight(
                    airline = crazyAirFlight.airline,
                    supplier = CRAZY_AIR_SUPPLIER,
                    fare = crazyAirFlight.price,
                    departureAirportCode = crazyAirFlight.departureAirportCode,
                    destinationAirportCode = crazyAirFlight.destinationAirportCode,
                    departureDate = crazyAirFlight.departureDate.atOffset(ZoneOffset.UTC).toInstant(),
                    arrivalDate = crazyAirFlight.arrivalDate.atOffset(ZoneOffset.UTC).toInstant(),
                )
            }
            .collect(Collectors.toList())
    }

    companion object {
        const val CRAZY_AIR_SUPPLIER = "CrazyAir"
    }
}
