package elements

interface MultiLine {
    fun toStringArray() : List<String>
}

interface SingleLine {
    override fun toString() : String
}