package ast.elements

import ast.*
import ast.elements.If.ElseIf
import patterns.cloneElements
import patterns.cloneWithCast
import patterns.serializers.ElementSerializer
import patterns.serializers.ElseIfSerializer
import patterns.serializers.IfSerializer
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
    var elseBody : Body? = null
) : BaseContainerElement(), BodyElement, MultiLine, WithRandomAutocomplete, Serializable<If> {

    override val serializer: ElementSerializer<If>
        get() = IfSerializer

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
            it.updateRelations()
        }

        // Тело основного условия проставление связи parent
        body.parent = this
        body.updateRelations()

        // Тело иначе проставление связи parent
        elseBody?.parent = this
        elseBody?.updateRelations()
    }

    override fun getChildElements(): List<BaseElement> = buildList {
        add(body)
        addAll(elseIfDeclarations.toList())
        elseBody?.let {
            add(it)
        }
    }

    override fun delete(element: BaseElement) {
        (element as? Body)?.let {
            when (it) {
                body -> (parent as BaseContainerElement).delete(this)
                elseBody -> elseBody = null
            }
        } ?: (element as? ElseIf)?.let {
            elseIfDeclarations.remove(it)
        } ?: throw ClassCastException("if delete: ${element::class}")
    }

    override fun clone(): BaseElement = this.copy(
        body = body.cloneWithCast(),
        elseIfDeclarations = elseIfDeclarations.cloneElements(),
        elseBody = elseBody?.cloneWithCast()
    ).apply { updateRelations() }

    /**
     * Элемент else if блока
     * @param stmt условие
     * @param body тело
     */
    data class ElseIf(
        var stmt: String = "",
        val body : Body = Body(),
    ) : BaseContainerElement(), MultiLine, WithRandomAutocomplete, Serializable<ElseIf> {

        override val serializer: ElementSerializer<ElseIf>
            get() = ElseIfSerializer

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

        override fun getChildElements(): List<BaseElement> = listOf(body)

        override fun delete(element: BaseElement) {
            ((element as? Body) ?: throw ClassCastException("Elif delete: ${element::class}")).takeIf { it == body }?.let {
                (parent as BaseContainerElement).delete(this)
            }
        }

        override fun clone(): BaseElement = this.copy(
            body = body.cloneWithCast()
        ).apply { updateRelations() }

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
