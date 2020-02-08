package com.alexsullivan.models

import com.alexsullivan.models.network.Guess
import com.alexsullivan.replace

fun newGame(gameCode: GameCode): Game {
    return Game(gameCode, randomWords(), GameStatus.Playing)
}

fun processGuess(oldGameState: Game, guess: Guess): Game {
    val word = oldGameState.words.first { it.text == guess.text }
    val updatedWord = word.copy(guessStatus = GuessStatus.GUESSED)
    val newWords = oldGameState.words.replace(word, updatedWord)
    return when (word.owner) {
        CardOwner.AssassinOwned -> Game(
            oldGameState.gameCode,
            newWords,
            GameStatus.GameOver(winner = guess.team.otherTeam)
        )
        else -> {
            val gameStatus = updatedStatus(newWords)
            Game(oldGameState.gameCode, newWords, gameStatus)
        }
    }
}

fun updatedStatus(words: List<Word>): GameStatus {
    val redWon =
        words.all { it.owner is CardOwner.TeamOwned && it.owner.team == Team.RED && it.guessStatus == GuessStatus.GUESSED }
    val blueWon =
        words.all { it.owner is CardOwner.TeamOwned && it.owner.team == Team.BLUE && it.guessStatus == GuessStatus.GUESSED }

    return when {
        redWon -> GameStatus.GameOver(winner = Team.RED)
        blueWon -> GameStatus.GameOver(winner = Team.BLUE)
        else -> GameStatus.Playing
    }
}

fun randomWords(): List<Word> {
    return listOf(
        Word("Grapefruit", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Banana", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Apple", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Wagon", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Wood", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Wife", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Love", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Cake", CardOwner.TeamOwned(Team.BLUE), GuessStatus.NOT_GUESSED),
        Word("Coffee", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Lousiana", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("France", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("King Henry", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Donald Trump", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Disaster", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Heavy", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Sweet", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Travel", CardOwner.TeamOwned(Team.RED), GuessStatus.NOT_GUESSED),
        Word("Nepal", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Chisso", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Rukh", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Kutta", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Pink", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Revolution", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Surprised", CardOwner.Unowned, GuessStatus.NOT_GUESSED),
        Word("Pokemon", CardOwner.AssassinOwned, GuessStatus.NOT_GUESSED)
    ).shuffled()
}