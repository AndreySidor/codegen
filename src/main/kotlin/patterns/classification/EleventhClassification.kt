package patterns.classification

import ast.NamedScope
import ast.elements.Class
import ast.elements.Declaration
import ast.elements.EnumClass
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.*
import patterns.prefix.PrefixGenerator

object EleventhClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.ELEVENTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Если префикс пустой
        if (prefix is PrefixGenerator.Prefix.Empty) {
            // Найти верхний по иерархии класс, содержащий идентификатор
            val containingClass = identifier.findByParents { it is Class }.firstOrNull() as? Class

            // Вернуть knowledge, если найдено любое поле класса или перечисление объявленное в этом классе, как поле, до места использования
            if (containingClass?.findAll(identifier, true) {
                    (it is Declaration.Variable && it.parent == containingClass)
                            || (it is EnumClass && it.parent == containingClass && it.getChildElements().isNotEmpty())
                }?.isNotEmpty() == true) {
                return knowledge
            }
        }

        // Если префикс именованный
        if (prefix is PrefixGenerator.Prefix.Named) {
            // ЕЕсли префикс располагается в глобальной области
            val inGlobal = prefix.toString().startsWith("::")
            val prefix = prefix.toString().removePrefix("::").removeSuffix("::")

            // Ищем содержащий класс по префиксу условия, учитывая факт расположения inGlobal,
            // а также содержание идентификатора в этом элементе
            val containingClassByPrefix = code.findAll(identifier) {
                if (it is NamedScope && it.fullName() == prefix && it is Class) {
                    ((inGlobal && it.containingContainer() is GlobalArea) || (!inGlobal && it.containingContainer() !is GlobalArea))
                            && identifier.isNestedIn(it)
                }
                false
            }.firstOrNull() as? Class

            // Вернуть knowledge, если найдено любое поле класса или перечисление объявленное в этом классе, как поле, до места использования
            if (containingClassByPrefix?.findAll(identifier, true) {
                    (it is Declaration.Variable && it.parent == containingClassByPrefix)
                            || (it is EnumClass && it.parent == containingClassByPrefix && it.getChildElements().isNotEmpty())
                }?.isNotEmpty() == true) {
                return knowledge
            }
        }

        return null
    }
}