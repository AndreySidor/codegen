package patterns.classification

import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.prefix.PrefixGenerator

object NinthClassification  : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.NINTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        return null
    }
}