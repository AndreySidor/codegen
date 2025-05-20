package patterns.serializers

import ast.Serializable
import ast.elements.Body
import ast.elements.BodyElement
import ast.elements.If
import patterns.Difficult
import patterns.PatternParser

object IfSerializer : ElementSerializer<If> {

    override val key: String
        get() = "if"

    override fun serialize(element: If): String = buildString {
        append("$key{\"${element.stmt}\";(${
            element.body.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.elseIfDeclarations.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.elseBody?.elements?.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            } ?: ""
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): If {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 4) {
            throw IllegalArgumentException("if must contained 4 parameters")
        }
        return If(
            stmt = PatternParser.fromStringParam(params[0]) ?: "",
            body = Body(
                elements = PatternParser.parseList<BodyElement>(params[1], difficult).toMutableList()
            ),
            elseIfDeclarations = PatternParser.parseList<If.ElseIf>(params[2], difficult).toMutableList(),
            elseBody = Body(
                elements = PatternParser.parseList<BodyElement>(params[3], difficult).toMutableList()
            ).takeIf { it.elements.isNotEmpty() }
        )
    }
}