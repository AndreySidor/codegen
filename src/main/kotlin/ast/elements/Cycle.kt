package ast.elements

import ast.*
import patterns.cloneWithCast
import patterns.serializers.ElementSerializer
import patterns.serializers.ForSerializer
import patterns.serializers.WhileSerializer
import templates.Templates.forStatements
import templates.Templates.statements

/**
 * Элемент цикла for и while
 * @param stmt строковое представление условия
 * @param body тело
 */
sealed class Cycle(
    var stmt : String,
    val body : Body
) : BaseContainerElement(), BodyElement, MultiLine, WithRandomAutocomplete {

    /**
     * Строковое представление типа цикла
     */
    abstract val type : String

    /**
     * Элемент цикла for
     */
    class For(
        stmt : String = "",
        body : Body = Body()
    ) : Cycle(stmt, body), Serializable<For> {

        override val serializer: ElementSerializer<For>
            get() = ForSerializer

        init {
            autocomplete()
            updateRelations()
        }
        override val type: String
            get() = FOR

        override fun updateRelations() {
            // Тело
            body.parent = this
            body.updateRelations()
        }

        override fun autocomplete() {
            // Условие цикла
            if (stmt.isEmpty()) {
                stmt = forStatements.random()
            }
        }

        override fun clone(): BaseElement = For(
            stmt = stmt,
            body = body.cloneWithCast()
        ).apply { updateRelations() }

        companion object {
            const val FOR = "for"
        }
    }

    /**
     * Элемент цикла while
     */
    class While(
        stmt : String = "",
        body : Body = Body()
    ) : Cycle(stmt, body), Serializable<While> {

        override val serializer: ElementSerializer<While>
            get() = WhileSerializer

        init {
            autocomplete()
            updateRelations()
        }
        override val type: String
            get() = WHILE

        override fun updateRelations() {
            // Тело
            body.parent = this
            body.updateRelations()
        }

        override fun autocomplete() {
            // Условие цикла
            if (stmt.isEmpty()) {
                stmt = statements.random()
            }
        }

        override fun clone(): BaseElement = While(
            stmt = stmt,
            body = body.cloneWithCast()
        ).apply { updateRelations() }

            companion object {
            const val WHILE = "while"
        }
    }

    override fun getChildElements(): List<BaseElement> = listOf(body)

    override fun delete(element: BaseElement) {
        ((element as? Body) ?: ClassCastException("Cycle delete: ${element::class}")).takeIf { it == body }?.let {
            (parent as BaseContainerElement).delete(this)
        }
    }

    override fun toStringArray(): List<String> = buildList {
        // Тип и условие
        add("$type ($stmt)")

        // Тело
        addAll(body.toStringArray())
    }
}
