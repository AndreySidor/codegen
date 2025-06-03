package patterns.template

import ast.elements.GlobalArea
import patterns.Difficult
import patterns.PatternParser
import patterns.prefix.PrefixGenerator

/**
 * Шаблонизатор
 */
object TemplateEngine {

    /**
     * Генерация данных для задачи из шаблона
     * @param template шаблон
     * @param difficult сложность
     * @return пара список [PrefixGenerator.Prefix] и объект [GlobalArea]
     */
    fun generate(template : String, difficult: Difficult?) : Pair<List<PrefixGenerator.Prefix>, GlobalArea> {
        val code = PatternParser.parseWithLinkers(template, difficult)
        if (code !is GlobalArea) {
            throw IllegalStateException("Error generate by template: root must be global")
        }
        val prefixes = PrefixGenerator.generate(code)
        return prefixes to code
    }

}