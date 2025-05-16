package ast.identifiers

import ast.elements.BodyElement
import ast.SingleLine

/**
 * Указатель на конкретное место в коде, может использоваться в задачах
 */
object UsageIdentifier : SingleLine, BodyElement {
    override fun toString(): String = "/*!!??!!*/"
}