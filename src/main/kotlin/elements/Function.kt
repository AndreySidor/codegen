package elements

import templates.Templates
import templates.Type
import templates.getValueBy

data class Function(
    val isStatic : Boolean = false,
    val isDefinition : Boolean = true,
    var returnType : Type? = null,
    var name : String? = null,
    val params : List<Declaration.Parameter>? = null,
    val body : Body? = null
) : MultiLine, SpaceElement, ClassElement, WithRandomAutocomplete {
    init {
        autocomplete()
    }

    override fun autocomplete() {
        if (returnType == null) {
            returnType = Type.getRandom()
        }
        if (name == null) {
            name = Templates.functionNames.random()
        }
    }

    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        result.add("${if (isStatic) "static " else ""}${returnType!!.value} ${name!!}(${
            params?.joinToString(", ") { 
                it.toString()
            } ?: ""
        })")
        if (isDefinition) {
            result.addAll(body?.toStringArray() ?: Body.empty())
            result.add(result.size - 1, "return ${getValueBy(returnType!!)};")
        } else {
            result.add(";");
        }
        return result
    }
}
