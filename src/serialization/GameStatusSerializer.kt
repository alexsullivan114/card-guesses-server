package com.alexsullivan.serialization

import com.alexsullivan.models.GameStatus
import com.alexsullivan.models.Team
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class GameStatusSerializer : JsonSerializer<GameStatus>() {
    override fun serialize(value: GameStatus, gen: JsonGenerator, serializers: SerializerProvider?) {
        val serializedString = when (value) {
            GameStatus.Playing -> "Playing"
            is GameStatus.GameOver -> if (value.winner == Team.Red) "RedWon" else "BlueWon"
        }

        gen.writeString(serializedString)
    }
}

