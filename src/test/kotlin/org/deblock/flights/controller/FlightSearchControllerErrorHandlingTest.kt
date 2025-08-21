package org.deblock.flights.controller

import org.deblock.flights.BaseTest
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class FlightSearchControllerErrorHandlingTest : BaseTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun performPost(requestJson: String) =
        mockMvc.perform(post("/api/flights")
            .content(requestJson)
            .contentType("application/json"))

    @Test
    fun `fails when departure after return`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-10",
                "numberOfPassengers": 3
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid request body"))
            .andExpect(jsonPath("$.details[0]", containsString("Return date cannot be before departure date")))
    }

    @Test
    fun `allows same day travel`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-15",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isOk)
    }

    @Test
    fun `fails when passengers exceed limit`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 6
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("There may be maximum 4 passengers")))
    }

    @Test
    fun `fails when origin missing`() {
        val requestJson = """
            {
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 1
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Missing request body parameter: origin")))
    }

    @Test
    fun `fails when origin invalid`() {
        val requestJson = """
            {
                "origin": "KRAKOW",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("IATA code for origin is invalid")))
    }

    @Test
    fun `fails when destination invalid`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DENPASAR",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("IATA code for destination is invalid")))
    }

    @Test
    fun `fails when origin and destination are identical`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "KRK",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 1
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Destination must be different than the origin")))
    }

    @Test
    fun `fails when zero passengers`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 0
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("There must be at least one passenger")))
    }

    @Test
    fun `fails when departureDate is invalid`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "WRONG_DATE",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Invalid format for parameter: departureDate")))
    }

    @Test
    fun `fails when departureDate in wrong format`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "15-09-2025",
                "returnDate": "2025-09-25",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Invalid format for parameter: departureDate")))
    }

    @Test
    fun `fails when returnDate invalid`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "WRONG_DATE",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Invalid format for parameter: returnDate")))
    }

    @Test
    fun `fails when returnDate in wrong format`() {
        val requestJson = """
            {
                "origin": "KRK",
                "destination": "DPS",
                "departureDate": "2025-09-15",
                "returnDate": "25/09/2025",
                "numberOfPassengers": 2
            }
        """.trimIndent()

        performPost(requestJson)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]", `is`("Invalid format for parameter: returnDate")))
    }
}
