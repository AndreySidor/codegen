package patterns.classification

import ast.NamedElement
import ast.elements.*
import ast.elements.Function
import ast.identifiers.UsageIdentifier
import patterns.findByParents
import patterns.findInParents
import patterns.prefix.PrefixGenerator

object SeventhClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.SEVENTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Найти статическую функцию в родителях идентификатора
        val staticFunctionInParent = identifier.findByParents {
            it is Function && it.isStatic
        }.firstOrNull()

        // если между статической функцией и идентификатором нет именованного контекста,
        // а также родитель функции это класс
        if (staticFunctionInParent?.parent is Class
            && identifier.findByParents(staticFunctionInParent) { it is NamedElement }.isEmpty()) {
            // Класс содержащий статическую функцию
            val containingClass = staticFunctionInParent.parent as Class

            // private и public элементы родительского класса
            val parentClassProtectedAndPublicElements = containingClass.parentClass?.let {
                it.protectedElements + it.publicElements
            } ?: emptyList()

            // Вернуть knowledge, если в классе или в элементах родительского класса есть не статические поля
            if (containingClass.getChildElements().count { it is Declaration.Variable && !it.isStatic } > 0
                || parentClassProtectedAndPublicElements.count { it is Declaration.Variable && !it.isStatic } > 0) {
                return knowledge
            }
        }

        // От статической функции и до глобальной области, не включая ее,
        // проверить наличие в родительских контекстах типа body не статических переменных,
        // либо наличие параметров функций, в случае успеха вернуть knowledge
        return if (staticFunctionInParent?.findInParents(to = code) {
            it is Declaration.Variable && !it.isStatic && it.parent is Body || it is Declaration.Parameter
        }?.isNotEmpty() == true) knowledge else null
    }
}