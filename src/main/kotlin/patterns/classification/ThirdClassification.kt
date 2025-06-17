package patterns.classification

import ast.elements.Class
import ast.elements.Declaration
import ast.elements.EnumClass
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.isNestedIn
import patterns.prefix.PrefixGenerator

object ThirdClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.THIRD

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Все классы, которые не содержат место использования
        val classesWithoutIdentifier = code.findAll {
            it is Class && !identifier.isNestedIn(it)
        }.toMutableList() as MutableList<Class>

        // Все классы, которые содержат место использования
        val classesContainingIdentifier = code.findAll {
            it is Class && identifier.isNestedIn(it)
        }.toMutableList() as MutableList<Class>

        // Если есть хоть один класс с private или protected полями
        val checkClassesBeforeIdentifier = classesWithoutIdentifier.count { classObject ->
            (classObject.protectedElements + classObject.privateElements).count { element ->
                element is Declaration.Variable
                        || (element is EnumClass && element.getChildElements().isNotEmpty())
            } > 0
        } > 0

        // Если есть хоть один класс, с родительским классом, у которого будут private поля
        val checkClassesContainingIdentifier = classesContainingIdentifier.count { classObject ->
            (classObject.parentClass?.privateElements ?: return@count false).count { element ->
                element is Declaration.Variable
                        || (element is EnumClass && element.getChildElements().isNotEmpty())
            } > 0
        } > 0

        return if (checkClassesBeforeIdentifier || checkClassesContainingIdentifier) knowledge else null
    }
}