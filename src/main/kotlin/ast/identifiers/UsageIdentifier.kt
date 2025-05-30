package ast.identifiers

import ast.BaseElement
import ast.SingleLine
import ast.elements.BodyElement
import ast.elements.SpaceElement

/**
 * Указатель на конкретное место в коде, может использоваться в задачах
 */
class UsageIdentifier : SingleLine, BodyElement, BaseElement(), SpaceElement {

    override fun clone(): BaseElement = UsageIdentifier()

    override fun toString(): String = "/*!!??!!*/"
}