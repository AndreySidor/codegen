package patterns.classification

import ast.NamedScope
import ast.elements.Declaration
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.findByParents
import patterns.prefix.PrefixGenerator

object FirstClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.FIRST

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Если префикс является глобальным
        if (prefix is PrefixGenerator.Prefix.Global
            || (prefix is PrefixGenerator.Prefix.Named && prefix.toString().startsWith("::"))) {

            // Вернуть knowledge, если найдена хоть одна переменная, у которой в родителях есть любой элемент кроме NamedScope
            if (code.findAll { element ->
                element is Declaration && element.findByParents(to = code) { it !is NamedScope }.isNotEmpty()
            }.isNotEmpty()) {
                return knowledge
            }
        }
        return null
    }
}