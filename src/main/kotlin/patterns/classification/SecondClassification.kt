package patterns.classification

import ast.BaseContainerElement
import ast.elements.Class
import ast.elements.Declaration
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.isNestedIn
import patterns.prefix.PrefixGenerator

object SecondClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.SECOND

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Вернуть knowledge, если найдена хоть одна переменная, объявленная после идентификатора,
        // и которая, не является полем класса, элементы которого содержат идентификатор
        return if (code.findAll(
            before = identifier,
            reverse = true
        ) { it is Declaration }.filterNot {
            it.parent is Class && identifier.isNestedIn(it.parent as BaseContainerElement)
        }.isNotEmpty()) knowledge else null
    }
}