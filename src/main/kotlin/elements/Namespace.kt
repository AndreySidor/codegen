package elements

interface SpaceElement

data class Namespace(
    var name : String? = null,
    val elements : List<SpaceElement>? = null
) : MultiLine, SpaceElement, WithRandomAutocomplete {
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
        result.add("namespace ${name!!}")
        result.add("{")
        elements?.forEach {
            when (it) {
                is SingleLine -> result.add(it.toString())
                is MultiLine -> result.addAll(it.toStringArray())
            }
        }
        result.add("}")
        return result
    }
}
