package patterns.serializers

import ast.Serializable
import ast.elements.Body
import ast.elements.BodyElement
import patterns.Difficult
import patterns.PatternParser

object BodySerializer : ElementSerializer<Body> {
    override val key: String
        get() = "body"

    override fun deserialize(pattern: String, difficult: Difficult?): Body {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 1) {
            throw IllegalArgumentException("body must contained 1 parameter")
        }
        return Body(
            elements = PatternParser.parseList<BodyElement>(params[0], difficult).toMutableList()
        )
    }

    override fun serialize(element: Body): String = buildString {
        append("$key{(${
            element.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }
}