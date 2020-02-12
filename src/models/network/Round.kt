package com.alexsullivan.models.network

import com.alexsullivan.models.Team

data class Round(val teamUp: Team, val clue: Clue?)

data class Clue(val text: String, val guessesLeft: Int)