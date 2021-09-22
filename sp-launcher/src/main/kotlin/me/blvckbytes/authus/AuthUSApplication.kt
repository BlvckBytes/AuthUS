package me.blvckbytes.authus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AuthUSApplication

fun main(args: Array<String>) {
    runApplication<AuthUSApplication>(*args)
}