package ast.elements

import ast.*
import patterns.cloneElements
import patterns.serializers.ElementSerializer
import patterns.serializers.GlobalAreaSerializer

/**
 * Элемент глобальной области
 * @param elements элементы голбальной области [SpaceElement]
 */
data class GlobalArea(
    val elements : MutableList<SpaceElement> = mutableListOf()
) : BaseContainerElement(), MultiLine, SingleLine, Serializable<GlobalArea> {

    override val serializer: ElementSerializer<GlobalArea>
        get() = GlobalAreaSerializer

    init {
        updateRelations()
    }

    override fun updateRelations() {
        // Проставление связей элементам глобальной области
        elements.forEach {
            (it as? BaseElement)?.parent = this
            (it as? BaseContainerElement)?.updateRelations()
        }
    }

    override fun getChildElements(): List<BaseElement> = elements.toList() as List<BaseElement>

    override fun delete(element: BaseElement) {
        (element as? SpaceElement)?.let {
            elements.remove(it)
        } ?: throw ClassCastException("GlobalArea delete: ${element::class}")
    }

    override fun clone(): BaseElement = GlobalArea(
        elements = elements.cloneElements()
    ).apply { updateRelations() }

    override fun toStringArray(): List<String> = buildList {
        // Элементы глобальной области
        elements.forEach {
            when (it) {
                is SingleLine -> add(it.toString())
                is MultiLine -> addAll(it.toStringArray())
            }

            // Добавление отступа между элементами в виде пустой строки
            add("")
        }

        // Удаление последней пустой строки
        if (size > 0) dropLast(1)
    }

    override fun toString(): String = elements.joinToString("\n\n") {
        when (it) {
            is SingleLine -> it.toString()
            is MultiLine -> it.toStringArray().joinToString("\n")
            else -> "\n"
        }
    }
}
