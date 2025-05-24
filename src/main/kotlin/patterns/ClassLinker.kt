package patterns

import ast.BaseContainerElement
import ast.elements.Class

/**
 * Объект соединяющий дочерний и родительский классы
 */
object ClassLinker {
    // Список найденных классов
    private val classes = mutableListOf<Class>()

    /**
     * Простановка связей
     * @param root корневой элемент AST кода
     */
    fun linkClasses(root: BaseContainerElement) {
        collectClasses(root)
        linkParents()

        // Очистка списка
        classes.clear()
    }

    /**
     * Сбор классов
     * @param element элемент типа [BaseContainerElement]
     */
    private fun collectClasses(element: BaseContainerElement) {
        // Добавляем элемент в список, если он является классом
        if (element is Class) {
            classes.add(element)
        }

        // Перебираем все дочерние элементы
        element.getChildElements().forEach { child ->
            (child as? BaseContainerElement)?.let { collectClasses(it) }
        }
    }

    /**
     * Простановка связей
     */
    private fun linkParents() {
        classes.forEach { element ->

            // Проверка на циклическую зависимость
            if (element.parentClassId  != null && element.identifier != null && element.parentClassId == element.identifier) {
                throw IllegalStateException("linkParents: circular relation: ${element.identifier}")
            }
            // Поиск и установка родительского класса
            element.parentClassId?.let { parentId ->
                classes.forEach {
                    if (parentId == it.identifier) {
                        element.parentClass = it
                    }
                }

                // Проверка, что родительский класс был найден
                if (element.parentClass == null) {
                    throw IllegalArgumentException("Parent class with identifier: ${parentId}, was not found")
                }
            }
        }
    }
}