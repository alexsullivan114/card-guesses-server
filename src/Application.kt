package com.alexsullivan

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        routing {
            post("/createGame") {
                call.respond(mapOf("code" to "ABCDE"))
            }
            get("/snippets") {
                call.respond(mapOf("OK" to true))
            }
            get("random/{min}/{max}") {
                val min = call.parameters["min"]?.toIntOrNull() ?: 0
                val max = call.parameters["max"]?.toIntOrNull() ?: 10
                val randomString = "${(min until max).shuffled().last()}"
                call.respond(mapOf("value" to randomString))
            }
        }
    }.start(wait = true)
}
