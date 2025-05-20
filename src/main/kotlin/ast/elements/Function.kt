package ast.elements

import ast.*
import patterns.serializers.ElementSerializer
import patterns.serializers.FunctionSerializer
import templates.Templates
import templates.Type

/**
 * Элемент функции / метода
 * @param isStatic является ли функия статической
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
    var name : String = "",
    val params : MutableList<Declaration.Parameter> = mutableListOf(),
    var body : Body? = null
) : BaseContainerElement(), MultiLine, SpaceElement, ClassElement, WithRandomAutocomplete, Serializable<Function> {

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
