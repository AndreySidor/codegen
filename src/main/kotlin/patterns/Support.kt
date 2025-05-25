package patterns

import ast.BaseContainerElement
import ast.BaseElement
import ast.NamedElement
import ast.NamedScope
import ast.elements.Class
import ast.elements.ClassElement
import ast.elements.Declaration
import ast.elements.Function

/**
 * Получение полного имени по именованным областям видимости
 * @return полное имя в строковой форме без учета глобального расширения и без (::) в конце
 */
fun NamedElement.fullName() : String = buildString {
    // Добавление имени элемента
    append(name)

    // Перебор по именованным областям видимости
    var current : BaseElement = this@fullName as BaseElement
    var parent = current.parent
    while (parent is NamedScope) {

        // Если родитель класс и наш элемент располагается в private или protected области видимости
        if (current is ClassElement && parent is Class) {
            if (!parent.publicElements.contains(current)) {
                break
            }
        }

        // Добавить имя области видимости в начало полного имени
        insert(0, "${parent.name}::")
        current = parent
        parent = parent.parent
    }
}

/**
 * Доступность контейнера по связи parent
 * @param from контейнер
 */
fun BaseElement.isNestedIn(from : BaseContainerElement) : Boolean {
    return if (this == from) true
    else this.parent?.isNestedIn(from) ?: false
}

/**
 * Получение содержащего контейнера, то есть крайний элемент из которого можно получить доступ к элементу
 * @return элемент типа [BaseContainerElement]
 */
fun BaseElement.containingContainer() : BaseContainerElement = when (parent) {
    is Class -> {
        // Если элемент с открытым модификатором видимости
        if ((parent as Class).publicElements.contains(this as ClassElement)) {

            // Переменная и метод должны быть статическими, для остальных элементов не важно
            when (this) {
                is Declaration.Variable -> {
                    if (isStatic) {
                        (parent as BaseElement).containingContainer()
                    } else {
                        parent as BaseContainerElement
                    }
                }
                is Function -> {
                    if (isStatic) {
                        (parent as BaseElement).containingContainer()
                    } else {
                        parent as BaseContainerElement
                    }
                }
                else -> (parent as BaseElement).containingContainer()
            }
        } else {
            parent as BaseContainerElement
        }
    }
    // Для перечисления и пространства имен
    is NamedScope -> {
        (parent as BaseElement).containingContainer()
    }
    else -> parent as BaseContainerElement
}