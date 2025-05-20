package patterns.serializers

import ast.Serializable
import ast.elements.Body
import ast.elements.BodyElement
import ast.elements.Declaration
import ast.elements.Function
import patterns.Difficult
import patterns.PatternParser

object FunctionSerializer : ElementSerializer<Function> {

    override val key: String
        get() = "function"

    override fun serialize(element: Function): String = buildString {
        append("$key{${element.isStatic};${element.isDefinition};\"${element.returnType}\";\"${element.name}\";(${
            element.params.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.body?.elements?.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            } ?: ""
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Function {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 6) {
            throw IllegalArgumentException("function must contained 6 parameters")
        }
        return Function(
            isStatic = PatternParser.fromBoolParam(params[0]) ?: false,
            isDefinition = PatternParser.fromBoolParam(params[1]) ?: false,
            returnType = PatternParser.fromStringParam(params[2]) ?: "",
            name = PatternParser.fromStringParam(params[3]) ?: "",
            params = PatternParser.parseList<Declaration.Parameter>(params[4], difficult).toMutableList(),
            body = Body(
                elements = PatternParser.parseList<BodyElement>(params[5], difficult).toMutableList()
            ).takeIf { it.elements.isNotEmpty() }
        )
    }
}