package me.blvckbytes.authus.util

import org.testcontainers.containers.MySQLContainer

/**
 * Database docker-container of latest mysql service
 */
object TestingMySQLContainer : MySQLContainer<TestingMySQLContainer>("mysql:latest") {

    init {
        // Spin up container on class construction
        this.start()
    }
}