package patterns

enum class Difficult(val identificator : String) {
    EASY("E"),
    MEDIUM("M"),
    HARD("H");

    companion object {
        fun by(value: String) : Difficult? = entries.firstOrNull { it.identificator == value }
    }
}