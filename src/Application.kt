package com.alexsullivan

import com.alexsullivan.models.*
import com.alexsullivan.models.network.Guess
import com.alexsullivan.models.network.Clue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import org.slf4j.event.Level
import java.util.*
import kotlin.streams.asSequence

@UseExperimental(KtorExperimentalAPI::class)
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val gameMap = mutableMapOf<GameCode, Game>()
    val wsConnections = Collections.synchronizedSet(LinkedHashSet<Pair<GameCode, DefaultWebSocketSession>>())
    fun setUpdatedGame(code: GameCode, game: Game) {
        gameMap[code] = game
        val objectMapper = ObjectMapper()
        wsConnections.filter { it.first == code }
            .map { it.second }
            .forEach {
                try {
                    it.outgoing.offer(Frame.Text(objectMapper.writeValueAsString(game)))
                } catch (e: Exception) {
                    println("Tried to push a frame to the socket and failed")
                }
            }
    }
    embeddedServer(Netty, port) {
        install(WebSockets)
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
            post("/game") {
                val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                val code = Random().ints(4, 0, source.length)
                    .asSequence()
                    .map(source::get)
                    .joinToString("")

                val gameCode = GameCode(code)
                val game = newGame(gameCode)
                setUpdatedGame(gameCode, game)

                call.respond(gameCode)
            }
            get("/game/{code}") {
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val game = gameMap[gameCode]
                if (game != null) {
                    call.respond(game)
                } else {
                    println("Couldn't find code in map $gameMap")
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            webSocket("/game/{code}/socket") {
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val connectionPair = Pair(gameCode, this)
                wsConnections += connectionPair
                val game = gameMap[gameCode]
                val objectMapper = ObjectMapper()
                game?.let { outgoing.offer(Frame.Text(objectMapper.writeValueAsString(it))) }
                for (frame in incoming) {
                    print("Frame: $frame")
                }
            }
            post("/game/{code}/guess") {
                val guess = call.receive<Guess>()
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val game = gameMap[gameCode]
                if (game != null && game.status == GameStatus.Playing) {
                    val updatedGame = processGuess(game, guess)
                    setUpdatedGame(gameCode, updatedGame)
                    call.respond(updatedGame)
                } else {
                    println("Couldn't find code in map $gameMap")
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post("/game/{code}/clue") {
                val clue = call.receive<Clue>()
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val game = gameMap[gameCode]
                if (game != null) {
                    val updatedGame = processClue(game, clue)
                    setUpdatedGame(gameCode, updatedGame)
                    call.respond(updatedGame)
                } else {
                    println("Couldn't find code in map $gameMap")
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            get("/game/{code}/clue") {
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val game = gameMap[gameCode]
                if (game != null && game.currentRound.clue != null) {
                    call.respond(game.currentRound.clue)
                } else {
                    println("Couldn't find code in map $gameMap")
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post("/game/{code}/master") {
                val code = call.parameters.getOrFail("code")
                val gameCode = GameCode(code)
                val game = gameMap[gameCode]
            }
        }
    }.start(wait = true)
}
