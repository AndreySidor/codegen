package ast.elements

import ast.BaseContainerElement
import ast.BaseElement
import ast.MultiLine
import ast.SingleLine

/**
 * Элемент может находиться внутри Body
 */
interface BodyElement

/**
 * Элемент тела функций, циклов, условий (фигурный скобки)
 * @param elements элементы тела список [BodyElement]
 */
data class Body(
    val elements : MutableList<BodyElement> = mutableListOf()
) : BaseContainerElement(), MultiLine, BodyElement {
    init {
        updateRelations()
    }

    override fun updateRelations() {
        // Простановка у потомков связи parent
        elements.forEach {
            (it as? BaseElement)?.parent = this
            (it as? BaseContainerElement)?.updateRelations()
        }
    }

    override fun toStringArray(): List<String> = buildList {
        // Открывающая фигурная скобка
        add("{")

        // Элементы тела
        elements.forEach { element ->
            when (element) {
                is SingleLine -> add(element.toString())
                is MultiLine -> addAll(element.toStringArray())
            }
        }

        // Закрывающая фигурная скобка
        add("}")
    }
}
