package dev.aurakai.auraframefx.serialization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for kotlinx.datetime.Instant
 * Converts Instant to/from ISO-8601 string representation
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    /**
     * Encodes an Instant as an ISO-8601 string.
     *
     * The Instant is converted with `Instant.toString()` and written to the provided encoder.
     *
     * @param value The Instant to encode; serialized as its ISO-8601 string representation.
     */
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

/**
 * Custom serializer for Any type - use with @Contextual annotation
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    /**
     * Encode an Any value as its string representation.
     *
     * Writes value.toString() to the encoder as a string primitive. Type information is not preserved;
     * deserialization will produce the string form, not the original object type.
     *
     * @param value The value to serialize; its `toString()` result is encoded.
     */
    override fun serialize(encoder: Encoder, value: Any) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }
}
