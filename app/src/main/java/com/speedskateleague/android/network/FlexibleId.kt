package com.speedskateleague.android.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

/**
 * Decodes an ID that the backend sometimes sends as a string and sometimes as an int,
 * always exposing it as a string. Mirrors SSLFlexibleID / FlexibleMeetID in SSLNetworking.swift.
 */
object FlexibleIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return runCatching { decoder.decodeString() }.getOrElse { UUID.randomUUID().toString() }
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element as? JsonPrimitive ?: return UUID.randomUUID().toString()
        return primitive.content
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
