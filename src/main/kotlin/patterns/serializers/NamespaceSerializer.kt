package patterns.serializers

import ast.Serializable
import ast.elements.Namespace
import ast.elements.SpaceElement
import patterns.Difficult
import patterns.PatternParser

object NamespaceSerializer : ElementSerializer<Namespace> {

    override val key: String
        get() = "namespace"

    override fun serialize(element: Namespace): String = buildString {
        append("$key{\"${element.name}\";(${
            element.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Namespace {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 2) {
            throw IllegalArgumentException("namespace must contained 2 parameters")
        }
        return Namespace(
            name = PatternParser.fromStringParam(params[0]) ?: "",
            elements = PatternParser.parseList<SpaceElement>(params[1], difficult).toMutableList()
        )
    }
}