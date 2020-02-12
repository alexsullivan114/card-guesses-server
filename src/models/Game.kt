package com.alexsullivan.models

import com.alexsullivan.models.network.Round
import com.alexsullivan.serialization.CardOwnerSerializer
import com.alexsullivan.serialization.GameStatusSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class Game(val gameCode: GameCode, val words: List<Word>, val status: GameStatus, val currentRound: Round)

data class Word(val text: String, val owner: CardOwner, val guessStatus: GuessStatus)

@JsonSerialize(using = CardOwnerSerializer::class)
sealed class CardOwner {
    class TeamOwned(val team: Team) : CardOwner()
    object Unowned : CardOwner()
    object AssassinOwned : CardOwner()
}

enum class Team {
    Blue, Red;

    val otherTeam: Team
        get() = if (this == Blue) Red else Blue
}

enum class GuessStatus { Guessed, NotGuessed }

@JsonSerialize(using = GameStatusSerializer::class)
sealed class GameStatus {
    object Playing : GameStatus()
    data class GameOver(val winner: Team) : GameStatus()
}

