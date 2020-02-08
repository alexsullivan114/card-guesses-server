package com.alexsullivan.models

import com.alexsullivan.serialization.CardOwnerSerializer
import com.alexsullivan.serialization.GameStatusSerializer
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class Game(@JsonUnwrapped val gameCode: GameCode, val words: List<Word>, val status: GameStatus)

data class Word(val text: String, val owner: CardOwner, val guessStatus: GuessStatus)

@JsonSerialize(using = CardOwnerSerializer::class)
sealed class CardOwner {
    class TeamOwned(val team: Team) : CardOwner()
    object Unowned : CardOwner()
    object AssassinOwned : CardOwner()
}

enum class Team {
    BLUE, RED;

    val otherTeam: Team
        get() = if (this == BLUE) RED else BLUE
}

enum class GuessStatus { GUESSED, NOT_GUESSED }

@JsonSerialize(using = GameStatusSerializer::class)
sealed class GameStatus {
    object Playing : GameStatus()
    data class GameOver(val winner: Team) : GameStatus()
}

