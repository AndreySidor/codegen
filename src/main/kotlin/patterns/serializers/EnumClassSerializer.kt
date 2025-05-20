package patterns.serializers

import ast.Serializable
import ast.elements.Declaration
import ast.elements.EnumClass
import patterns.Difficult
import patterns.PatternParser

object EnumClassSerializer : ElementSerializer<EnumClass> {

    override val key: String
        get() = "enum"

    override fun serialize(element: EnumClass): String = buildString {
        append("$key{\"${element.name}\";(${
            element.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): EnumClass {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 2) {
            throw IllegalArgumentException("enum must contained 2 parameters")
        }
        return EnumClass(
            name = PatternParser.fromStringParam(params[0]) ?: "",
            elements = PatternParser.parseList<Declaration.EnumConstant>(params[1], difficult).toMutableList()
        )
    }
}