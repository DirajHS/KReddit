package com.diraj.kreddit.utils

import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.RedditObjectData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

class RedditObjectDataSerializer : KSerializer<RedditObjectData> {
    @ExperimentalSerializationApi
    @InternalSerializationApi
    override val descriptor: SerialDescriptor = buildSerialDescriptor("RedditObjectData",
        PolymorphicKind.SEALED) {
        element<BaseModel>("replies")
    }

    override fun deserialize(decoder: Decoder): RedditObjectData {
        // Decoder -> JsonDecoder
        require(decoder is JsonDecoder) // this class can be decoded only by Json
        // JsonDecoder -> JsonElement
        val element = decoder.decodeJsonElement()
        // JsonElement -> value
        if (element is JsonObject && (element["replies"] == null || element["replies"]!! is JsonPrimitive)) {
            return decoder.json.decodeFromJsonElement<RedditObjectData.RedditObjectDataWithoutReplies>(
                element
            )
        }
        return decoder.json.decodeFromJsonElement<RedditObjectData.RedditObjectDataWithReplies>(element)
    }

    override fun serialize(encoder: Encoder, value: RedditObjectData) {
        // Encoder -> JsonEncoder
        require(encoder is JsonEncoder) // This class can be encoded only by Json
        // value -> JsonElement
        val element = when (value) {
            is RedditObjectData.RedditObjectDataWithReplies -> encoder.json.encodeToJsonElement(value)
            is RedditObjectData.RedditObjectDataWithoutReplies -> encoder.json.encodeToJsonElement(value)
        }
        // JsonElement -> JsonEncoder
        encoder.encodeJsonElement(element)
    }
}