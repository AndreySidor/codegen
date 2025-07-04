package ast.elements

import ast.*
import patterns.cloneElements
import patterns.serializers.BodySerializer
import patterns.serializers.ElementSerializer

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
) : BaseContainerElement(), MultiLine, BodyElement, Serializable<Body> {

    override val serializer: ElementSerializer<Body>
        get() = BodySerializer

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

    override fun getChildElements(): List<BaseElement> = elements.toList() as List<BaseElement>

    override fun delete(element: BaseElement) {
        (element as? BodyElement)?.let {
            elements.remove(it)
        } ?: throw ClassCastException("body delete: ${element::class}")
    }

    override fun clone(): BaseElement = Body(
        elements = elements.cloneElements()
    ).apply { updateRelations() }

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
