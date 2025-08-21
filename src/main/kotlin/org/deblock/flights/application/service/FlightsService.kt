package org.deblock.flights.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.deblock.flights.adapter.inbound.dto.FlightRequestDTO
import org.deblock.flights.adapter.inbound.dto.FlightResponseDTO
import org.deblock.flights.adapter.inbound.mapper.FlightsMapper
import org.deblock.flights.adapter.outbound.supplier.FlightSearchSupplier
import org.deblock.flights.domain.port.FlightsServiceUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FlightsService(
    private val flightSearchSuppliers: List<FlightSearchSupplier>,
) : FlightsServiceUseCase {

    override fun searchFlights(request: FlightRequestDTO): FlightResponseDTO {
        val flightRequest = FlightsMapper.toSearchRequest(request)

        val flights = runBlocking {
            val deferredFlights =
                flightSearchSuppliers.map { supplier ->
                    async(Dispatchers.IO) {
                        try {
                            logger.info("Sending a request to supplier ${supplier.javaClass.simpleName}")
                            val result = supplier.searchFlights(flightRequest)
                            logger.info("Got result from ${supplier.javaClass.simpleName}")
                            result
                        } catch (e: Exception) {
                            logger.error("Error while fetching flights from ${supplier.javaClass.simpleName}", e)
                            emptyList()
                        }
                    }
                }
            deferredFlights.awaitAll().flatten().sortedBy { it.fare }
        }

        return FlightsMapper.map(flights)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FlightsService::class.java)
    }
}
