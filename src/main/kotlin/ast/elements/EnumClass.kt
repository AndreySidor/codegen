package ast.elements

import ast.*
import patterns.serializers.ElementSerializer
import patterns.serializers.EnumClassSerializer
import templates.Templates

/**
 * Элемент перечисления
 * @param name имя
 * @param elements элементы перечисления [Declaration.EnumConstant]
 */
data class EnumClass(
    override var name : String = "",
    val elements : MutableList<Declaration.EnumConstant> = mutableListOf()
) : BaseContainerElement(), MultiLine,
    SpaceElement, BodyElement, ClassElement, WithRandomAutocomplete, Serializable<EnumClass>, NamedScope {

    override val serializer: ElementSerializer<EnumClass>
        get() = EnumClassSerializer

    init {
        autocomplete()
        updateRelations()
    }

    override fun autocomplete() {
        // Имя
        if (name.isEmpty()) {
            name = Templates.enumNames.random()
        }
    }

    override fun updateRelations() {
        // Проставление связи parent элементам перечисления
        elements.forEach {
            (it as? BaseElement)?.parent = this
        }
    }

    override fun getChildElements(): List<BaseElement> = elements

    override fun toStringArray(): List<String> = buildList {
        // Имя
        add("enum $name")

        // Открывающая фигурная скобка
        add("{")

        // Элементы перечисления
        if (elements.size > 1) {
            addAll(elements.joinToString(", ") {
                it.toString().filterNot { char -> char.isWhitespace() }
            }.split(" "))
        } else if (elements.size == 1) {
            add(elements.first().toString())
        }

        // Закрывающая фигурная скобка
        add("};")
    }
}
