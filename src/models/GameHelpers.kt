package com.alexsullivan.models

import com.alexsullivan.models.network.Guess
import com.alexsullivan.replace

fun newGame(gameCode: GameCode): Game {
    return Game(gameCode, randomWords(), GameStatus.Playing)
}

fun processGuess(oldGameState: Game, guess: Guess): Game {
    val word = oldGameState.words.first { it.text == guess.text }
    val updatedWord = word.copy(guessStatus = GuessStatus.Guessed)
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
        words.all { it.owner is CardOwner.TeamOwned && it.owner.team == Team.Red && it.guessStatus == GuessStatus.Guessed }
    val blueWon =
        words.all { it.owner is CardOwner.TeamOwned && it.owner.team == Team.Blue && it.guessStatus == GuessStatus.Guessed }

    return when {
        redWon -> GameStatus.GameOver(winner = Team.Red)
        blueWon -> GameStatus.GameOver(winner = Team.Blue)
        else -> GameStatus.Playing
    }
}

fun randomWords(): List<Word> {
    return listOf(
        Word("Grapefruit", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Banana", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Apple", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Wagon", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Wood", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Wife", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Love", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Cake", CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed),
        Word("Coffee", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Lousiana", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("France", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("King Henry", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Donald Trump", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Disaster", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Heavy", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Sweet", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Travel", CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed),
        Word("Nepal", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Chisso", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Rukh", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Kutta", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Pink", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Revolution", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Surprised", CardOwner.Unowned, GuessStatus.NotGuessed),
        Word("Pokemon", CardOwner.AssassinOwned, GuessStatus.NotGuessed)
    ).shuffled()
}