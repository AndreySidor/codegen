package patterns.serializers

import ast.elements.Declaration
import patterns.Difficult
import patterns.PatternParser

object ParameterSerializer : ElementSerializer<Declaration.Parameter> {

    override val key: String
        get() = "parameter"

    override fun serialize(element: Declaration.Parameter): String = buildString {
        append("$key{${element.isConst};\"${element.type}\";\"${element.name}\"}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Declaration.Parameter {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 3) {
            throw IllegalArgumentException("parameter must contained 3 parameters")
        }
        return Declaration.Parameter(
            isConst = PatternParser.fromBoolParam(params[0]) ?: false,
            type = PatternParser.fromStringParam(params[1]) ?: "",
            name = PatternParser.fromStringParam(params[2]) ?: ""
        )
    }
}