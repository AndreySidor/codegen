package ast

import patterns.serializers.ElementSerializer

/**
 * Сериализация объекта в шаблон
 * @property T класс объекта, должен быть наследником [BaseElement]
 */
interface Serializable<T : BaseElement> {

    /**
     * Сериализатор объекта
     */
    val serializer : ElementSerializer<T>

    /**
     * Сериализация
     */
    fun serialize() : String = serializer.serialize(this as T)
}