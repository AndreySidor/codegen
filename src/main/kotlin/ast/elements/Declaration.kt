package ast.elements

import ast.*
import patterns.serializers.ElementSerializer
import patterns.serializers.EnumConstantSerializer
import patterns.serializers.ParameterSerializer
import patterns.serializers.VariableSerializer
import templates.Templates
import templates.Type

/**
 * Элемент объявления переменной, поля класса, элемента перечисления, параметра функции или метода
 */
sealed class Declaration : BaseElement(), SingleLine, WithRandomAutocomplete, NamedElement {

    /**
     * Элемент объявления переменной
     * @param isStatic является ли переменная статичной
     * @param isConst является ли переменная константной
     * @param type тип переменной (может генерироваться автоматически, если есть в enum [Type])
     * @param name имя переменной
     * @param isDefinition есть ли определение переменной
     * @param definition определение переменной (может генерироваться в зависимости от типа, если тип UNDEFINED, то null)
     */
    data class Variable(
        var isStatic : Boolean = false,
        var isConst : Boolean = false,
        var type : String = "",
        override var name : String = "",
        var isDefinition : Boolean = false,
        var definition : String? = null
    ) : BodyElement, SpaceElement, ClassElement, Declaration(), Serializable<Variable> {

        override val serializer: ElementSerializer<Variable>
            get() = VariableSerializer

        init {
            autocomplete()
        }

        override fun autocomplete() {
            // Тип
            if (type.isEmpty()) {
                type = Type.random().value
            }

            // Имя
            if (name.isEmpty()) {
                name = Templates.variableNames.random()
            }

            // Определение
            if (isDefinition && definition == null) {
                definition = Type.by(type).definition()
            }
        }

        override fun toString(): String = buildString {
            // Статичность
            if (isStatic) append("static ")

            // Константность
            if (isConst) append("const ")

            // Тип и имя
            append("$type $name")

            // Определение
            if (isDefinition && definition != null) append(" = ${definition};") else append(";")
        }
    }

    /**
     * Элемент элемента перечисления
     * @param name имя элемента перечисления
     */
    data class EnumConstant(override var name : String = "") : Declaration(), Serializable<EnumConstant> {

        override val serializer: ElementSerializer<EnumConstant>
            get() = EnumConstantSerializer

        init {
            autocomplete()
        }

        override fun autocomplete() {
            // Имя
            if (name.isEmpty()) {
                name = Templates.enumConstantNames.random()
            }
        }

        override fun toString(): String = name
    }

    /**
     * Элемент параметра функции / метода
     * @param isConst является ли параметр константным
     * @param type тип параметра (может генерироваться автоматически, если есть в enum [Type])
     * @param name имя параметра
     */
    data class Parameter(
        var isConst : Boolean = false,
        var type : String = "",
        override var name : String = ""
    ) : Declaration(), Serializable<Parameter> {

        override val serializer: ElementSerializer<Parameter>
            get() = ParameterSerializer

        init {
            autocomplete()
        }

        override fun autocomplete() {
            // Тип
            if (type.isEmpty()) {
                type = Type.random().value
            }

            // Имя
            if (name.isEmpty()) {
                name = Templates.variableNames.random()
            }
        }

        override fun toString(): String = buildString {
            // Константность
            if (isConst) append("const ")

            // Тип и имя
            append("$type $name")
        }
    }
}