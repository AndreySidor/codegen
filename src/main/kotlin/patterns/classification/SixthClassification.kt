package patterns.classification

import ast.BaseContainerElement
import ast.NamedScope
import ast.elements.Declaration
import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.fullName
import patterns.prefix.PrefixGenerator
import kotlin.math.min

object SixthClassification : Classification {
    override val knowledge: ScopeKnowledge
        get() = ScopeKnowledge.SIXTH

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

            // Разбиваем префиксы по :: и переворачиваем для перебора
            val stringPrefixSplit = stringPrefix.split("::").reversed()
            val prefixesSplit = prefixes.map { it.split("::").reversed() }

            // Проверяем каждый префикс кода
            prefixesSplit.forEach { pref ->

                // Если часть какой-либо префикс не совпал с префиксом в условии по имени, то возвращаем knowledge
                for (i in 0..<min(pref.count(), stringPrefixSplit.count())) {
                    if (pref[i] != stringPrefixSplit[i]) {
                        return knowledge
                    }
                }
            }
        }

        return null
    }
}