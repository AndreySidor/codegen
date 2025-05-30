package ast.elements

import ast.*
import patterns.cloneElements
import patterns.cloneWithCast
import patterns.serializers.ElementSerializer
import patterns.serializers.FunctionSerializer
import templates.Templates
import templates.Type

/**
 * Элемент функции / метода
 * @param isStatic является ли функция статической
 * @param isDefinition есть ли у функции определение
 * @param returnType возвращаемый тип функции
 * @param name имя
 * @param params параметры функции [Declaration.Parameter]
 * @param body тело
 */
data class Function(
    var isStatic : Boolean = false,
    var isDefinition : Boolean = false,
    var returnType : String = "",
    override var name : String = "",
    val params : MutableList<Declaration.Parameter> = mutableListOf(),
    var body : Body? = null
) : BaseContainerElement(), MultiLine, SpaceElement, ClassElement, WithRandomAutocomplete,
    Serializable<Function>, NamedElement {

    override val serializer: ElementSerializer<Function>
        get() = FunctionSerializer

    init {
        autocomplete()
        updateRelations()
    }

    override fun autocomplete() {
        // Возвращаемый тип
        if (Type.by(returnType) == Type.UNDEFINED) {
            returnType = Type.random().value
        }

        // Имя
        if (name.isEmpty()) {
            name = Templates.functionNames.random()
        }

        // Тело (просто создаем body, если есть определение, но нет элементов)
        if (isDefinition && body == null) {
            body = Body()
        }
    }

    override fun updateRelations() {
        // Параметры функции
        params.forEach {
            (it as? BaseElement)?.parent = this
        }

        // Тело
        body?.parent = this
        body?.updateRelations()
    }

    override fun getChildElements(): List<BaseElement> = buildList {
        addAll(params.toList())
        body?.let {
            add(it)
        }
    }

    override fun delete(element: BaseElement) {
        (element as? Declaration.Parameter)?.let {
            params.remove(it)
        } ?: (element as? Body)?.let {
            if (it == body) {
                (parent as BaseContainerElement).delete(this)
            }
        } ?: throw ClassCastException("function delete: ${element::class}")
    }

    override fun clone(): BaseElement = this.copy(
        params = params.cloneElements(),
        body = body?.cloneWithCast()
    ).apply { updateRelations() }

    override fun toStringArray(): List<String> = buildList {
        // Статичность, возвращаемый тип, имя, параметры и ;, если прототип
        add("${if (isStatic) "static " else ""}${returnType} ${name}(${
            params.takeIf { it.isNotEmpty() }?.joinToString(", ") { 
                it.toString()
            } ?: ""
        })${if (!isDefinition) ";" else ""}")

        // Определение, если есть
        if (isDefinition) {
            body?.toStringArray()?.let { addAll(it) }
            add(size - 1, "return ${Type.by(returnType).definition() ?: ""};")
        }
    }
}
