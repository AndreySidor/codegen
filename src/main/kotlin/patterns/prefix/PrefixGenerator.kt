package patterns.prefix

import ast.BaseContainerElement
import ast.BaseElement
import ast.NamedScope
import ast.elements.*
import ast.identifiers.UsageIdentifier
import patterns.containingContainer
import patterns.fullName
import patterns.isNestedIn

/**
 * Генератор префиксов для задачи
 */
object PrefixGenerator {

    /**
     * Генерация всех возможных префиксов
     *
     * Примечание:
     *
     * В коде обязательно должен присутствовать UsageIdentifier
     *
     * @param root элемент типа [GlobalArea]
     * @return список [Prefix]
     */
    fun generate(root : GlobalArea) : List<Prefix> {
        // Итоговый список префиксов, добавляем пустой префикс по умолчанию
        val prefixes = mutableListOf<Prefix>(Prefix.Empty)

        val rootElements = root.getChildElements()

        // Именованные контексты в коде до UsageIdentifier
        val namedScopes : List<NamedScope>
        var identifier : UsageIdentifier? = null

        findNamedScopeBeforeUsageIdentifier(root).let {
            namedScopes = it.first
            it.second?.let { id ->
                identifier = id
                // Выкидываем исключение, если UsageIdentifier не был найден
            } ?: throw IllegalArgumentException("generate: Usage identifier not found")
        }

        // Если в коде есть глобальные переменные или enum в глобальной области видимости, то добавляем глобальный префикс
        if (rootElements.count {
            it is Declaration.Variable || (it is EnumClass && it.getChildElements().isNotEmpty())
        } > 0) {
            prefixes.add(Prefix.Global)
        }

        // Для каждого именованного контекста проводим проверку на доступность из UsageIdentifier и наличие верных ответов
        namedScopes.forEach {
            // Находим содержащий контейнер именованной области
            val container = (it as BaseElement).containingContainer()

            // Является ли контейнер глобальной областью видимости
            var isGlobal = false
            if (container is GlobalArea) {
                isGlobal = true
            }

            when (it) {
                is Class -> {
                    // Наличие статических переменных с открытым модификатором видимости, а также enum с элементами там же
                    if (identifier!!.isNestedIn(container)
                        && it.publicElements.count { publicElement ->
                            (publicElement is Declaration.Variable && publicElement.isStatic)
                                    || (publicElement is EnumClass && publicElement.getChildElements().isNotEmpty())
                        } > 0) {

                        // Создаем именованный префикс и добавляем :: в начало, если корневой элемент располагается в глобальной области видимости
                        prefixes.add(Prefix.Named("${
                            if (isGlobal) Prefix.Global.toString() else ""
                        }${it.fullName()}::"))
                    }
                }
                is Namespace -> {
                    // Наличие переменных и enum с элементами
                    if (identifier!!.isNestedIn(container)
                        && it.getChildElements().count { namespaceElement ->
                            (namespaceElement is Declaration.Variable)
                                    || (namespaceElement is EnumClass && namespaceElement.getChildElements().isNotEmpty())
                        } > 0) {

                        // Создаем именованный префикс и добавляем :: в начало, если корневой элемент располагается в глобальной области видимости
                        prefixes.add(Prefix.Named("${
                            if (isGlobal) Prefix.Global.toString() else ""
                        }${it.fullName()}::"))
                    }
                }
                is EnumClass -> {
                    // Наличие элементов
                    if (identifier!!.isNestedIn(container)
                        && it.getChildElements().isNotEmpty()) {

                        // Создаем именованный префикс и добавляем :: в начало, если корневой элемент располагается в глобальной области видимости
                        prefixes.add(Prefix.Named("${
                            if (isGlobal) Prefix.Global.toString() else ""
                        }${it.fullName()}::"))
                    }
                }
            }
        }

        // Удаляем повторяющиеся префиксы
        val uniquePrefixes = mutableListOf<Prefix>()
        for (item in prefixes) {
            if (uniquePrefixes.count { it.toString() == item.toString() } == 0) {
                uniquePrefixes.add(item)
            }
        }

        // Возвращаем уникальные префиксы
        return uniquePrefixes
    }

    /**
     * Найти все именованные области видимости до UsageIdentifier
     * @return пара список [NamedScope] и UsageIdentifier
     */
    private fun findNamedScopeBeforeUsageIdentifier(container: BaseContainerElement): Pair<List<NamedScope>, UsageIdentifier?> {
        val result = mutableListOf<NamedScope>()
        var findUsageIdentifier : UsageIdentifier? = null

        for (element in container.getChildElements()) {
            when (element) {
                is UsageIdentifier -> {
                    findUsageIdentifier = element
                    break
                }

                is NamedScope -> {
                    result.add(element)
                }
            }
            if (element is BaseContainerElement) {
                val (nestedScopes, foundInNested) = findNamedScopeBeforeUsageIdentifier(element)
                result.addAll(nestedScopes)
                if (foundInNested != null) {
                    findUsageIdentifier = foundInNested
                    break
                }
            }
        }

        return result to findUsageIdentifier
    }

    /**
     * Префикс
     */
    sealed interface Prefix {

        /**
         * Пустой префикс
         */
        object Empty : Prefix {
            override fun toString(): String = ""
        }

        /**
         * Глобальный префикс
         */
        object Global : Prefix {
            override fun toString(): String = "::"
        }

        /**
         * Именованный префикс
         */
        data class Named(val prefix: String) : Prefix {
            override fun toString(): String = prefix
        }
    }
}