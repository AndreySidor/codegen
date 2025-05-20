package patterns.serializers

import ast.Serializable
import ast.elements.Body
import ast.elements.BodyElement
import ast.elements.Cycle
import patterns.Difficult
import patterns.PatternParser

object ForSerializer : ElementSerializer<Cycle.For> {

    override val key: String
        get() = "for"

    override fun serialize(element: Cycle.For): String = buildString {
        append("$key{\"${element.stmt}\";(${
            element.body.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Cycle.For {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 2) {
            throw IllegalArgumentException("for must contained 2 parameters")
        }
        return Cycle.For(
            stmt = PatternParser.fromStringParam(params[0]) ?: "",
            body = Body(
                elements = PatternParser.parseList<BodyElement>(params[1], difficult).toMutableList()
            )
        )
    }
}