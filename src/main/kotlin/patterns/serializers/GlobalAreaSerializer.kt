package patterns.serializers

import ast.Serializable
import ast.elements.GlobalArea
import ast.elements.SpaceElement
import patterns.Difficult
import patterns.PatternParser

object GlobalAreaSerializer : ElementSerializer<GlobalArea> {

    override val key: String
        get() = "global"

    override fun serialize(element: GlobalArea): String = buildString {
        append("$key{(${
            element.elements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): GlobalArea {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 1) {
            throw IllegalArgumentException("global must contained 1 parameter")
        }
        return GlobalArea(
            elements = PatternParser.parseList<SpaceElement>(params[0], difficult).toMutableList()
        )
    }
}