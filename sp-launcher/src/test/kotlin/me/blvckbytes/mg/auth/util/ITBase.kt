package me.blvckbytes.authus.util

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import me.blvckbytes.authus.MGAuthApplication
import me.blvckbytes.authus.infrastructure.DatabaseConnection
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.sql.DataSource
import kotlin.random.Random

@ActiveProfiles("test") // Activating test profile
@RunWith(SpringRunner::class)
@SpringBootTest(
    // Running on a random port
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,

    // Loading context configuration and bootstrapper
    classes = [ ITBase.ContextConfiguration::class, MGAuthApplication::class ]
)
open class ITBase {

    companion object {
        private val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    }

    // Wire local server port into field
    @LocalServerPort
    var port = 0

    // Every test case will assign the random port to rest-assured
    // and clear all existing data to completely reset the environment
    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost/api"
        DatabaseConnection.clearAllTables()
    }

    // Configuration used to override context for testing environment
    @Configuration
    open class ContextConfiguration {

        // Configure the datasource to be wired with the testing-container
        @Bean
        open fun dataSource(): DataSource? {
            return DataSourceBuilder.create()
                .url(TestingMySQLContainer.jdbcUrl)
                .username(TestingMySQLContainer.username)
                .password(TestingMySQLContainer.password)
                .build()
        }
    }

    protected fun makeRequest(): RequestSpecification {
        return given().contentType(ContentType.JSON)
    }

    protected fun generateString(length: Int = 32): String {
        return (1..length)
            .map { Random.nextInt(0, chars.size) }
            .map { chars[it] }
            .joinToString("")
    }

    protected fun generateInt(min: Int = 0, max: Int = 10000): Int {
        return Random.nextInt(min, max)
    }

    protected fun generateDouble(min: Double = 0.0, max: Double = 10000.0): Double {
        return Random.nextDouble(min, max)
    }
}