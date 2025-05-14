package elements

import templates.Templates
import templates.Type
import templates.getValueBy

sealed class Declaration : SingleLine, WithRandomAutocomplete {

    data class Variable(
        val isStatic : Boolean = false,
        val isConst : Boolean = false,
        var type : Type? = null,
        var name : String? = null,
        val isDefinition : Boolean = false,
        var definition : String? = null
    ) : BodyElement, SpaceElement, ClassElement, Declaration() {
        init {
            autocomplete()
        }

        override fun autocomplete() {
            if (type == null) {
                type = Type.getRandom()
            }
            if (name == null) {
                name = Templates.variableNames.random()
            }
            if (isDefinition && definition == null) {
                definition = getValueBy(type!!)
            }
        }

        override fun toString(): String {
            var result = ""
            result += if (isStatic) "static " else ""
            result += if (isConst) "const " else ""
            result += "${type!!.value} ${name!!}"
            result += if (isDefinition) " = ${definition!!};" else ";"
            return result
        }
    }

    data class EnumConstant(var name : String? = null) : Declaration() {
        init {
            autocomplete()
        }

        override fun autocomplete() {
            if (name == null) {
                name = Templates.enumConstantNames.random()
            }
        }

        override fun toString(): String {
            return name!!
        }
    }

    data class Parameter(
        val isConst : Boolean = false,
        var type : Type? = null,
        var name : String? = null
    ) : Declaration() {
        init {
            autocomplete()
        }

        override fun autocomplete() {
            if (type == null) {
                type = Type.getRandom()
            }
            if (name == null) {
                name = Templates.variableNames.random()
            }
        }

        override fun toString(): String {
            var result = ""
            result += if (isConst) "const " else ""
            result += "${type!!.value} ${name!!}"
            return result
        }
    }
}