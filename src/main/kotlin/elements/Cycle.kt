package elements

import kotlin.random.Random

data class Cycle(
    var type : Type? = null,
    var stmt : String? = null,
    val body : Body? = null
) : BodyElement, MultiLine, WithRandomAutocomplete {
    init {
        autocomplete()
    }

    override fun autocomplete() {
        if (type == null) {
            type = Type.getRandom()
        }
        if (stmt == null) {
            stmt = when (type!!) {
                Type.FOR -> forStatements.random()
                Type.WHILE -> statements.random()
            }
        }
    }

    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        result.add("${type!!.value} ($stmt)")
        result.addAll(body?.toStringArray() ?: Body.empty())
        return result
    }

    companion object {
        enum class Type(val value : String) {
            FOR("for"),
            WHILE("while");

            companion object {
                fun getRandom() : Type = entries[Random.nextInt(0, entries.size)]
            }
        }
    }
}
