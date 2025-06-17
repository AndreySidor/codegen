package patterns.classification

import ast.elements.Body
import ast.elements.Declaration
import ast.elements.Function
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.containingContainer
import patterns.findAll
import patterns.isNestedIn
import patterns.prefix.PrefixGenerator

object EighthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.EIGHTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Найти все переменные, которые содержаться в блоках и параметрах функций
        val variables = code.findAll {
            val containingContainer = it.containingContainer()
            it is Declaration
                    && (containingContainer is Body || containingContainer is Function)
        }

        // Для каждой переменной
        variables.forEach { variable ->
            // Вернуть knowledge, если идентификатор не содержится в содержащем контейнере переменной
            if (!identifier.isNestedIn(variable.containingContainer())) {
                return knowledge
            }
        }

        return null
    }
}