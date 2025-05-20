package patterns.serializers

import ast.elements.Declaration
import patterns.Difficult
import patterns.PatternParser

object VariableSerializer : ElementSerializer<Declaration.Variable> {

    override val key: String
        get() = "variable"

    override fun serialize(element: Declaration.Variable): String = buildString {
        append("$key{${element.isStatic};${element.isConst};${element.isDefinition};\"${element.type}\";\"${element.name}\";${
            element.definition?.let { "\"${element.definition}\"" } ?: ""
        }")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Declaration.Variable {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 6) {
            throw IllegalArgumentException("variable must contained 6 parameters")
        }
        return Declaration.Variable(
            isStatic = PatternParser.fromBoolParam(params[0]) ?: false,
            isConst = PatternParser.fromBoolParam(params[1]) ?: false,
            isDefinition = PatternParser.fromBoolParam(params[2]) ?: false,
            type = PatternParser.fromStringParam(params[3]) ?: "",
            name = PatternParser.fromStringParam(params[4]) ?: "",
            definition = PatternParser.fromStringParam(params[5])
        )
    }
}