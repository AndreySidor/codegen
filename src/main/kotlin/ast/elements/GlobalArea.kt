package ast.elements

import ast.*

/**
 * Элемент глобальной области
 * @param elements элементы голбальной области [SpaceElement]
 */
data class GlobalArea(
    val elements : MutableList<SpaceElement> = mutableListOf()
) : BaseContainerElement(), MultiLine, SingleLine {

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
