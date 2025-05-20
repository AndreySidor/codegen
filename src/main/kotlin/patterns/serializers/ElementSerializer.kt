package patterns.serializers

import ast.BaseElement
import patterns.Difficult

/**
 * Сериализатор для элементов, типа [BaseElement]
 */
interface ElementSerializer<T : BaseElement> {

    /**
     * Ключ, который обозначает данный элемент в паттерне
     */
    val key : String

    /**
     * @param element элемент типа [BaseElement]
     * @return строковый паттерн, типа key{}
     */
    fun serialize(element : T) : String

    /**
     * @param pattern паттерн типа {} (без key)
     * @param difficult сложность паттерна, нужно, для определения, надобности дочерних элементов
     * @return элемент типа [BaseElement]
     */
    fun deserialize(pattern : String, difficult : Difficult?) : T
}