package elements

data class GlobalArea(
    val elements : List<SpaceElement>
) : MultiLine, SingleLine {
    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        elements.forEach {
            when (it) {
                is SingleLine -> result.add(it.toString())
                is MultiLine -> result.addAll(it.toStringArray())
            }
            result.add("")
        }
        result.dropLast(1)
        return result
    }

    override fun toString(): String = elements.joinToString("\n\n") {
        when (it) {
            is SingleLine -> it.toString()
            is MultiLine -> it.toStringArray().joinToString("\n")
            else -> "\n"
        }
    }
}
