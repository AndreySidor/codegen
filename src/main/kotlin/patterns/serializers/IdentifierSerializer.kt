package patterns.serializers

import ast.identifiers.UsageIdentifier
import patterns.Difficult

object IdentifierSerializer : ElementSerializer<UsageIdentifier> {
    override val key: String
        get() = "identifier"

    override fun deserialize(pattern: String, difficult: Difficult?): UsageIdentifier = UsageIdentifier()

    override fun serialize(element: UsageIdentifier): String = "$key{}"
}