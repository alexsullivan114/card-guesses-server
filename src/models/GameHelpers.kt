package com.alexsullivan.models

import com.alexsullivan.models.network.Clue
import com.alexsullivan.models.network.Guess
import com.alexsullivan.models.network.Round
import com.alexsullivan.replace

fun newGame(gameCode: GameCode): Game {
    return Game(
        gameCode, randomWords(), GameStatus.Playing, Round(Team.Red, null),
        redMasterSelected = false, blueMasterSelected = false
    )
}

fun processClue(oldGameState: Game, clue: Clue): Game {
    return oldGameState.copy(currentRound = oldGameState.currentRound.copy(clue = clue))
}

fun processGuess(oldGameState: Game, guess: Guess): Game {
    val word = oldGameState.words.first { it.text == guess.text }
    val updatedWord = word.copy(guessStatus = GuessStatus.Guessed)
    val newWords = oldGameState.words.replace(word, updatedWord)
    return if (guess.team != oldGameState.currentRound.teamUp || oldGameState.currentRound.clue == null) {
        // If it's not your turn to guess then do nothing
        oldGameState.copy()
    } else {
        when (word.owner) {
            CardOwner.AssassinOwned -> Game(
                oldGameState.gameCode,
                newWords,
                GameStatus.GameOver(winner = guess.team.otherTeam),
                Round(guess.team.otherTeam, null),
                oldGameState.redMasterSelected,
                oldGameState.blueMasterSelected
            )
            else -> {
                val gameStatus = updatedStatus(newWords)
                val round = updateRound(oldGameState.currentRound, word)
                Game(
                    oldGameState.gameCode,
                    newWords,
                    gameStatus,
                    round,
                    oldGameState.redMasterSelected,
                    oldGameState.blueMasterSelected
                )
            }
        }
    }
}

private fun updateRound(previousRound: Round, guessedWord: Word): Round {
    val hint = previousRound.clue ?: return previousRound.copy()
    val guessedOtherTeamsWord = when (guessedWord.owner) {
        is CardOwner.TeamOwned -> previousRound.teamUp != guessedWord.owner.team
        else -> false
    }
    return if (hint.guessesLeft <= 1 || guessedOtherTeamsWord) {
        previousRound.copy(teamUp = previousRound.teamUp.otherTeam, clue = null)
    } else {
        previousRound.copy(clue = hint.copy(guessesLeft = hint.guessesLeft - 1))
    }
}

private fun updatedStatus(words: List<Word>): GameStatus {
    val redWon =
        words.filter { it.owner is CardOwner.TeamOwned && it.owner.team == Team.Red }
            .all { it.guessStatus == GuessStatus.Guessed }
    val blueWon =
        words.filter { it.owner is CardOwner.TeamOwned && it.owner.team == Team.Blue }
            .all { it.guessStatus == GuessStatus.Guessed }

    return when {
        redWon -> GameStatus.GameOver(winner = Team.Red)
        blueWon -> GameStatus.GameOver(winner = Team.Blue)
        else -> GameStatus.Playing
    }
}

private fun randomWords(): List<Word> {
    val shuffledWords = words.shuffled().subList(0, 25)
    val redWords = shuffledWords.subList(0, 9)
        .map { Word(it, CardOwner.TeamOwned(Team.Red), GuessStatus.NotGuessed) }
    val blueWords = shuffledWords.subList(9, 17)
        .map { Word(it, CardOwner.TeamOwned(Team.Blue), GuessStatus.NotGuessed) }
    val unownedWords = shuffledWords.subList(17, 24)
        .map { Word(it, CardOwner.Unowned, GuessStatus.NotGuessed) }
    val assasinWord = Word(shuffledWords.get(24), CardOwner.AssassinOwned, GuessStatus.NotGuessed)

    return (redWords + blueWords + unownedWords + assasinWord).shuffled()
}