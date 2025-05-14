package elements

data class Enum(
    var name : String? = null,
    val elements : List<Declaration.EnumConstant>? = null
) : MultiLine, SpaceElement, BodyElement, ClassElement, WithRandomAutocomplete {
    init {
        autocomplete()
    }

    override fun autocomplete() {
        if (name == null) {
            name = names.random().apply { this[0].uppercase() }
        }
    }

    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        result.add("enum ${name!!}")
        result.add("{")
        elements?.let {
            if (elements.size > 1) {
                result.addAll(elements.joinToString(", ") { it.toString() }.split(" "))
            } else {
                result.add(elements.first().toString())
            }
        }
        result.add("};")
        return result
    }
}
