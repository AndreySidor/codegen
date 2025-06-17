package patterns.classification

import ast.BaseContainerElement
import ast.NamedScope
import ast.elements.EnumClass
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.containingContainer
import patterns.findAll
import patterns.fullName
import patterns.isNestedIn
import patterns.prefix.PrefixGenerator

object TwelfthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.TWELFTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Возвращаем knowledge, если префикс пустой и найден хоть один enum из родительского контекста которого,
        // можно было бы получить доступ к идентификатору
        if (prefix is PrefixGenerator.Prefix.Empty
            && code.findAll(identifier) {
                it is EnumClass && it.getChildElements().isNotEmpty() && identifier.isNestedIn(it.parent as BaseContainerElement)
            }.isNotEmpty()) {
            return knowledge
        }

        // Вернуть knowledge, если префикс глобальный и найден хоть один enum, располагающийся до идентификатора в глобальной области
        if (prefix is PrefixGenerator.Prefix.Global) {
            return if (code.findAll(identifier) {
                it is EnumClass && it.getChildElements().isNotEmpty() && it.parent is GlobalArea
            }.isNotEmpty()) knowledge else null
        }

        // Условие, если префикс именованный
        if (prefix is PrefixGenerator.Prefix.Named) {
            // ЕЕсли префикс располагается в глобальной области
            val inGlobal = prefix.toString().startsWith("::")
            val prefix = prefix.toString().removePrefix("::").removeSuffix("::")

            // Ищем элемент по префиксу условия, учитывая факт расположения inGlobal, а также содержание идентификатора в containingContainer
            val namedScope = code.findAll(identifier) {
                if (it is NamedScope && it.fullName() == prefix) {
                    ((inGlobal && it.containingContainer() is GlobalArea) || (!inGlobal && it.containingContainer() !is GlobalArea))
                            && identifier.isNestedIn(it.containingContainer())
                }
                false
            }.firstOrNull() as? BaseContainerElement

            // Возвращаем knowledge, если в найденной области видимости есть enum
            namedScope?.let { element ->
                if (element.getChildElements().count { it is EnumClass && it.getChildElements().isNotEmpty() } > 0) {
                    return knowledge
                }
            }
        }

        return null
    }
}