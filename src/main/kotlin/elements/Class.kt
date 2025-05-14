package elements

interface ClassElement

data class Class(
    var name : String? = null,
    val parent : Class? = null,
    val privateElements : List<ClassElement>? = null,
    val protectedElement : List<ClassElement>? = null,
    val publicElements : List<ClassElement>? = null
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
        result.add("class ${name!!} ${parent?.let { " : ${it.name}" }}")
        result.add("{")
        publicElements?.let {
            result.add("public:")
            publicElements.forEach {
                when (it) {
                    is SingleLine -> result.add(it.toString())
                    is MultiLine -> result.addAll(it.toStringArray())
                }
            }
        }
        protectedElement?.let {
            result.add("protected:")
            protectedElement.forEach {
                when (it) {
                    is SingleLine -> result.add(it.toString())
                    is MultiLine -> result.addAll(it.toStringArray())
                }
            }
        }
        privateElements?.let {
            result.add("private:")
            privateElements.forEach {
                when (it) {
                    is SingleLine -> result.add(it.toString())
                    is MultiLine -> result.addAll(it.toStringArray())
                }
            }
        }
        result.add("};")
        return result
    }
}
