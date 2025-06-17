package patterns.classification

import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.prefix.PrefixGenerator

object TenthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.TENTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        return null
    }
}