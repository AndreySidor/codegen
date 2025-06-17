package patterns.classification

import ast.BaseContainerElement
import ast.NamedScope
import ast.elements.Declaration
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.fullName
import patterns.prefix.PrefixGenerator

object FifthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.FIFTH

    override fun check(prefix: PrefixGenerator.Prefix, code: GlobalArea, identifier: UsageIdentifier): ScopeKnowledge? {
        // Если префикс содержит именованные области видимости
        if (prefix is PrefixGenerator.Prefix.Named) {
            // Строковое представление префикса задачи без :: в начале и в конце
            val stringPrefix = prefix.toString().removePrefix("::").removeSuffix("::")

            // Получаем все префиксы именованных контекстов кода и удаляем префикс задачи
            val prefixes = code.findAll { element ->
                element is NamedScope && (element as BaseContainerElement).getChildElements().count { it is Declaration } > 0
            }.map {
                (it as NamedScope).fullName()
            }.toMutableList().apply {
                remove(stringPrefix)
            }

            // Проверяем каждый префикс кода
            prefixes.forEach {
                // Если префикс условия задачи заканчивается на целый префикс из кода, то возвращаем knowledge
                if (stringPrefix.endsWith(it) && stringPrefix != it) {
                    return knowledge
                }
            }
        }
        return null
    }
}