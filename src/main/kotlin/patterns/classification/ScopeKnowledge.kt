package patterns.classification

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ScopeKnowledge.Serializer::class)
enum class ScopeKnowledge(val description : String) {
    FIRST(""),
    SECOND(""),
    THIRD(""),
    FOURTH(""),
    FIFTH(""),
    SIXTH(""),
    SEVENTH(""),
    EIGHTH(""),
    NINTH(""),
    TENTH(""),
    ELEVENTH(""),
    TWELFTH("");

    object Serializer : KSerializer<ScopeKnowledge> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ScopeKnowledge", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: ScopeKnowledge) {
            encoder.encodeInt(value.ordinal + 1)
        }

        override fun deserialize(decoder: Decoder): ScopeKnowledge {
            val ordinal = decoder.decodeInt() - 1
            return ScopeKnowledge.entries[ordinal]
        }
    }
}