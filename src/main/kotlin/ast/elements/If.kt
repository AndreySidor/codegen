package ast.elements

import ast.BaseContainerElement
import ast.BaseElement
import ast.MultiLine
import ast.WithRandomAutocomplete
import templates.Templates.preload
import templates.Templates.statements

/**
 * Элемент условия (if, else if, else)
 * @param stmt условие основного if
 * @param body тело основного if
 * @param elseIfDeclarations else if тела [ElseIf]
 * @param elseBody else тело
 */
data class If(
    var stmt : String = "",
    val body : Body = Body(),
    val elseIfDeclarations : MutableList<ElseIf> = mutableListOf(),
    val elseBody : Body? = null
) : BaseContainerElement(), BodyElement, MultiLine, WithRandomAutocomplete {
    init {
        autocomplete()
        updateRelations()
    }

    override fun autocomplete() {
        // Условие
        if (stmt.isEmpty()) {
            stmt = statements.random()
        }
    }

    override fun updateRelations() {
        // else if блоки проставление связи parent
        elseIfDeclarations.forEach {
            it.parent = this
        }

        // Тело основного условия проставление связи parent
        body.parent = this
        body.updateRelations()

        // Тело иначе проставление связи parent
        elseBody?.parent = this
        elseBody?.updateRelations()
    }

    /**
     * Элемент else if блока
     * @param stmt условие
     * @param body тело
     */
    data class ElseIf(
        var stmt: String = "",
        val body : Body = Body(),
    ) : BaseContainerElement(), MultiLine, WithRandomAutocomplete {
        init {
            autocomplete()
            updateRelations()
        }

        override fun autocomplete() {
            // Условие
            if (stmt.isEmpty()) {
                stmt = statements.random()
            }
        }

        override fun updateRelations() {
            // Тело проставление связи parent
            body.parent = this
            body.updateRelations()
        }

        override fun toStringArray(): List<String> = buildList {
            // Условие
            add("else if ($stmt)")

            // Тело
            addAll(body.toStringArray())
        }
    }

    override fun toStringArray(): List<String> = buildList {
        // Основное условие
        add("if ($stmt)")

        // Основное тело
        addAll(body.toStringArray())

        // else if блоки
        elseIfDeclarations.forEach { addAll(it.toStringArray()) }

        // else блок, если есть
        elseBody?.let {
            add("else")
            addAll(it.toStringArray())
        }
    }
}
