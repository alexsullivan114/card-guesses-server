package com.alexsullivan.models

import com.alexsullivan.models.network.Clue
import com.alexsullivan.models.network.Guess
import com.alexsullivan.models.network.Round
import com.alexsullivan.replace

fun newGame(gameCode: GameCode): Game {
    return Game(gameCode, randomWords(), GameStatus.Playing, Round(Team.Red, null))
}

fun processClue(oldGameState: Game, clue: Clue): Game {
    return oldGameState.copy(currentRound = oldGameState.currentRound.copy(clue = clue))
}

fun processGuess(oldGameState: Game, guess: Guess): Game {
    val word = oldGameState.words.first { it.text == guess.text }
    val updatedWord = word.copy(guessStatus = GuessStatus.Guessed)
    val newWords = oldGameState.words.replace(word, updatedWord)
    return if (guess.team != oldGameState.currentRound.teamUp || oldGameState.currentRound.clue == null) {
        oldGameState.copy()
    } else {
        when (word.owner) {
            CardOwner.AssassinOwned -> Game(
                oldGameState.gameCode,
                newWords,
                GameStatus.GameOver(winner = guess.team.otherTeam),
                Round(guess.team.otherTeam, null)
            )
            else -> {
                val gameStatus = updatedStatus(newWords)
                val round = updateRound(oldGameState.currentRound)
                Game(oldGameState.gameCode, newWords, gameStatus, round)
            }
        }
    }
}

private fun updateRound(previousRound: Round): Round {
    val hint = previousRound.clue ?: return previousRound.copy()
    return if (hint.guessesLeft <= 1) {
        previousRound.copy(teamUp = previousRound.teamUp.otherTeam, clue = null)
    } else {
        previousRound.copy(clue = hint.copy(guessesLeft = hint.guessesLeft - 1))
    }
}

private fun updatedStatus(words: List<Word>): GameStatus {
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

private fun randomWords(): List<Word> {
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