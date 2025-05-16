package ast.elements

import ast.*
import templates.Templates

/**
 * Элемент может находится в пространстве имен
 */
interface SpaceElement

/**
 * Элемент пространства имен
 * @param name имя
 * @param elements элементы [SpaceElement]
 */
data class Namespace(
    var name : String = "",
    val elements : MutableList<SpaceElement> = mutableListOf()
) : BaseContainerElement(), MultiLine, SpaceElement, WithRandomAutocomplete {
    init {
        autocomplete()
        updateRelations()
    }

    override fun autocomplete() {
        // Имя
        if (name.isEmpty()) {
            name = Templates.namespaceNames.random()
        }
    }

    override fun updateRelations() {
        // Элементы пространства имен проставление связи parent
        elements.forEach {
            (it as? BaseElement)?.parent = this
            (it as? BaseContainerElement)?.updateRelations()
        }
    }

    override fun toStringArray(): List<String> = buildList {
        // Имя
        add("namespace $name")

        // Открывающая фигурная скобка
        add("{")

        // Элементы пространства имен
        elements.forEach {
            when (it) {
                is SingleLine -> add(it.toString())
                is MultiLine -> addAll(it.toStringArray())
            }
        }

        // Закрывающая фигурная скобка
        add("}")
    }
}
