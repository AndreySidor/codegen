package patterns.serializers

import ast.elements.Declaration
import patterns.Difficult
import patterns.PatternParser

object EnumConstantSerializer : ElementSerializer<Declaration.EnumConstant> {

    override val key: String
        get() = "enum_constant"

    override fun serialize(element: Declaration.EnumConstant): String = buildString {
        append("$key{\"${element.name}\"}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Declaration.EnumConstant {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 1) {
            throw IllegalArgumentException("enum_constant must contained 1 parameter")
        }
        return Declaration.EnumConstant(
            name = PatternParser.fromStringParam(params[0]) ?: ""
        )
    }
}