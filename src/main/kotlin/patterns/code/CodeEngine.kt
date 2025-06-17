package patterns.code

import ast.elements.GlobalArea
import patterns.PatternParser
import patterns.prefix.PrefixGenerator

/**
 * Генератор из кода
 */
object CodeEngine {

    /**
     * Генерация данных для задачи из кода
     * @param template шаблон
     * @return список (пара (список [PrefixGenerator.Prefix] и объект [GlobalArea]) )
     */
    fun generate(template : String) : List<Pair<List<PrefixGenerator.Prefix>, GlobalArea>> {
        val result = mutableListOf<Pair<List<PrefixGenerator.Prefix>, GlobalArea>>()

        // Парсинг шаблона
        val code = PatternParser.parseWithLinkers(template, null)
        if (code !is GlobalArea) {
            throw IllegalStateException("Error generate by template: root must be global")
        }

        // Генерация кодов
        val codes = CodeGenerator.generate(code)

        // Генерация префиксов для кодов
        codes.forEach {
            val prefixes = PrefixGenerator.generate(it)
            result.add(prefixes to it)
        }
        return result
    }
}