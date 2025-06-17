package patterns.classification

import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.prefix.PrefixGenerator

/**
 * Интерфейс классификации задания по ошибке или знанию
 */
interface Classification {

    /**
     * Проверяемое знание
     */
    val knowledge : ScopeKnowledge

    /**
     * Проверка наличия этого знания в задании
     * @param prefix префикс задачи
     * @param code код задачи
     * @param identifier идентификатор задачи
     * @return если ошибка или знание было обнаружено, то knowledge, иначе null
     */
    fun check(prefix : PrefixGenerator.Prefix, code : GlobalArea, identifier : UsageIdentifier) : ScopeKnowledge?
}