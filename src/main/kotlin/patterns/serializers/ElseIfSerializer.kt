package patterns.serializers

import ast.Serializable
import ast.elements.Body
import ast.elements.BodyElement
import ast.elements.If
import patterns.Difficult
import patterns.PatternParser

object ElseIfSerializer : ElementSerializer<If.ElseIf> {

    override val key: String
        get() = "elif"

    override fun serialize(element: If.ElseIf): String = buildString {
        append("$key{\"${element.stmt}\";(${
            element.body.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): If.ElseIf {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 2) {
            throw IllegalArgumentException("elif must contained 2 parameters")
        }
        return If.ElseIf(
            stmt = PatternParser.fromStringParam(params[0]) ?: "",
            body = Body(
                elements = PatternParser.parseList<BodyElement>(params[1], difficult).toMutableList()
            )
        )
    }
}