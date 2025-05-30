package ast

import ast.elements.GlobalArea
import randomString

/**
 * Базовый самостоятельный элемент кода
 */
abstract class BaseElement {
    /**
     * Уникальный идентификатор элемента (8-ми символьная строка)
     */
    var id : String = randomString(8)
        private set

    /**
     * Контейнер, предок элемента (также BaseElement)
     *
     * Примечания:
     * 1. При попытке доступа к полю, когда оно null - исключение
     * 2. При попытке установления поля в null - исключение
     * 3. При получении свойства у [GlobalArea] - null
     * 4. При попытке установки свойства для [GlobalArea] - исключение
     */
    var parent : BaseElement? = null
        get() = if (field != null) field
        else if (this is GlobalArea) null
        else throw UninitializedPropertyAccessException("Parent must be initialized before access")
        set(value) {
            if (this is GlobalArea) {
                throw IllegalArgumentException("GlobalArea cannot have a parent")
            }
            field = value ?: throw IllegalArgumentException("Parent cannot be set to null")
        }

    /**
     * Клонирование элемента
     *
     * ВКЛЮЧАЕТ ГЛУБОКОЕ КЛОНИРОВАНИЕ СО ВСЕМИ ВЛОЖЕННЫМИ ЭЛЕМЕНТАМИ, ЗА ИСКЛЮЧНИЕМ РОДИТЕЛЬСКОГО КЛАССА, ОН НЕ КЛОНИРУЕТСЯ
     */
    abstract fun clone() : BaseElement
}