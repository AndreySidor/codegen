package patterns.classification

import ast.elements.Class
import ast.elements.Declaration
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.findByParents
import patterns.isNestedIn
import patterns.prefix.PrefixGenerator

object FourthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.FOURTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Все классы, которые не содержат место использования
        val classesWithoutIdentifier = code.findAll {
            it is Class && !identifier.isNestedIn(it)
        }.toMutableList() as MutableList<Class>

        // Все классы, которые содержат место использования,
        // кроме самого верхнего, который непосредственно содержит идентификатор
        val classesContainingIdentifier = (code.findByParents { it is Class }.toMutableList() as MutableList<Class>).apply {
            if (this.isNotEmpty()) {
                this.removeFirst()
            }
        }

        // Если есть хоть один класс с не статическими public полями
        val checkClassesBeforeIdentifier = classesWithoutIdentifier.count { classObject ->
            classObject.publicElements.count { element ->
                element is Declaration.Variable && !element.isStatic
            } > 0
        } > 0

        // Если есть хоть один класс с не статическими полями, или есть родитель с не статическими public или protected полями
        val checkClassesContainingIdentifier = classesContainingIdentifier.count { classObject ->
            classObject.getChildElements().count { element ->
                element is Declaration.Variable && !element.isStatic
            } > 0 || classObject.parentClass?.let { parent ->
                (parent.publicElements + parent.protectedElements).count { element ->
                    element is Declaration.Variable && !element.isStatic
                } > 0
            } ?: false
        } > 0

        return if (checkClassesBeforeIdentifier || checkClassesContainingIdentifier) knowledge else null
    }
}