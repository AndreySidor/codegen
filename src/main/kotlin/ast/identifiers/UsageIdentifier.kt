package ast.identifiers

import ast.BaseElement
import ast.SingleLine
import ast.elements.BodyElement
import ast.elements.SpaceElement

/**
 * Указатель на конкретное место в коде, может использоваться в задачах
 */
object UsageIdentifier : SingleLine, BodyElement, BaseElement(), SpaceElement {
    override fun toString(): String = "/*!!??!!*/"
}