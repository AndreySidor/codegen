package ast.identifiers

import ast.BaseElement
import ast.Serializable
import ast.SingleLine
import ast.elements.BodyElement
import ast.elements.SpaceElement
import patterns.serializers.ElementSerializer
import patterns.serializers.IdentifierSerializer

/**
 * Указатель на конкретное место в коде, может использоваться в задачах
 */
class UsageIdentifier : SingleLine, BodyElement, BaseElement(), SpaceElement, Serializable<UsageIdentifier> {

    override val serializer: ElementSerializer<UsageIdentifier>
        get() = IdentifierSerializer

    override fun clone(): BaseElement = UsageIdentifier()

    override fun toString(): String = "/*..??..*/"
}