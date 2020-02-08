package com.alexsullivan.models

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class GameCode(@JsonUnwrapped val code: String)