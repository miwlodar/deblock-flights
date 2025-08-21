package org.deblock.flights

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
abstract class BaseTest {
    protected val wireMockServer: WireMockServer = WireMockServer(8095)

    @BeforeAll
    fun beforeAll() {
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }
}
