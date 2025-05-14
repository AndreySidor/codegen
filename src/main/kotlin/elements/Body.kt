package elements

interface BodyElement

data class Body(
    val elements : List<BodyElement>? = null
) : MultiLine, BodyElement {
    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        result.add("{")
        elements?.forEach { element ->
            when (element) {
                is SingleLine -> result.add(element.toString())
                is MultiLine -> result.addAll(element.toStringArray())
            }
        }
        result.add("}")
        return result
    }

    companion object {
        fun empty() : List<String> = listOf("{", "}")
    }
}
