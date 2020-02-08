package com.alexsullivan

import com.alexsullivan.models.Game
import com.alexsullivan.models.GameCode
import com.alexsullivan.models.network.Guess
import com.alexsullivan.models.newGame
import com.alexsullivan.models.processGuess
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import org.slf4j.event.Level
import java.util.*
import kotlin.streams.asSequence

@UseExperimental(KtorExperimentalAPI::class)
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val gameMap = mutableMapOf<GameCode, Game>()
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
        install(CallLogging) {
            level = Level.INFO
        }

        routing {
            get("") {
                call.respond("OK")
            }
            route("/game") {
                post {
                    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    val code = Random().ints(4, 0, source.length)
                        .asSequence()
                        .map(source::get)
                        .joinToString("")

                    val gameCode = GameCode(code)
                    gameMap[gameCode] = newGame(gameCode)

                    call.respond(gameCode)
                }
                get("{code}") {
                    val code = call.parameters.getOrFail("code")
                    val gameCode = GameCode(code)
                    val game = gameMap[gameCode]
                    if (game != null) {
                        call.respond(game)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
                post("/{code}/guess") {
                    val guess = call.receive<Guess>()
                    val code = call.parameters.getOrFail("code")
                    val gameCode = GameCode(code)
                    val game = gameMap[gameCode]
                    if (game != null) {
                        val updatedGame = processGuess(game, guess)
                        gameMap[gameCode] = updatedGame
                        call.respond(updatedGame)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }.start(wait = true)
}
