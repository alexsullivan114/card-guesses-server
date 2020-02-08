package com.alexsullivan.serialization

import com.alexsullivan.models.CardOwner
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class CardOwnerSerializer : JsonSerializer<CardOwner>() {
    override fun serialize(value: CardOwner, gen: JsonGenerator, serializers: SerializerProvider?) {
        val serializedString = when (value) {
            is CardOwner.TeamOwned -> value.team.toString()
            CardOwner.Unowned -> "Unowned"
            CardOwner.AssassinOwned -> "AssassinOwned"
        }

        gen.writeString(serializedString)
    }
}

