package ast.identifiers

import ast.BaseElement
import ast.Serializable
import ast.SingleLine
import ast.elements.Body
import ast.elements.BodyElement
import ast.elements.Function
import ast.elements.SpaceElement
import patterns.findAll
import patterns.serializers.ElementSerializer
import patterns.serializers.IdentifierSerializer
import selectMiddleElement

/**
 * Указатель на конкретное место в коде, может использоваться в задачах
 */
class UsageIdentifier : SingleLine, BodyElement, BaseElement(), SpaceElement, Serializable<UsageIdentifier> {

    override val serializer: ElementSerializer<UsageIdentifier>
        get() = IdentifierSerializer

    override fun clone(): BaseElement = UsageIdentifier()

    override fun toString(): String = "/*..??..*/"

    fun setIn(function: Function) : Function {
        if (function.body == null) {
            return function.apply {
                body = Body(
                    elements = mutableListOf(this@UsageIdentifier)
                )
                updateRelations()
            }
        }

        val bodies = function.body?.findAll { it is Body } as? List<Body>

        val body = bodies?.selectMiddleElement() ?: function.body!!

        body.elements.add(body.elements.count() / 2, this@UsageIdentifier)

        return function.apply { updateRelations() }
    }
}