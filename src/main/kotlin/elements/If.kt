package elements

import templates.Templates.statements

data class If(
    var stmt : String? = null,
    val body : Body? = null,
    val elseIfDeclarations : List<ElseIf>? = null,
    val elseBody : Body? = null
) : BodyElement, MultiLine, WithRandomAutocomplete {
    init {
        autocomplete()
    }

    override fun autocomplete() {
        if (stmt == null) {
            stmt = statements.random()
        }
    }

    data class ElseIf(
        var stmt: String? = null,
        val body : Body? = null,
    ) : MultiLine, WithRandomAutocomplete {
        init {
            autocomplete()
        }

        override fun autocomplete() {
            if (stmt == null) {
                stmt = statements.random()
            }
        }

        override fun toStringArray(): List<String> {
            val result = mutableListOf<String>()
            result.add("else if (${stmt!!})")
            result.addAll(body?.toStringArray() ?: Body.empty())
            return result
        }
    }

    override fun toStringArray(): List<String> {
        val result = mutableListOf<String>()
        result.add("if (${stmt!!})")
        result.addAll(body?.toStringArray() ?: Body.empty())
        elseIfDeclarations?.forEach { result.addAll(it.toStringArray()) }
        elseBody?.let {
            result.add("else")
            result.addAll(it.toStringArray())
        }
        return result
    }
}
