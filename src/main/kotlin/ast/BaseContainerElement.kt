package ast

/**
 * Базовые элемент являющийся контейнером (может содержать другие элементы)
 */
abstract class BaseContainerElement : BaseElement() {
    /**
     * Функция для обновления связей между родителем и его потомками (parent)
     */
    abstract fun updateRelations()
}