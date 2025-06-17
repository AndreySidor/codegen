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

/**
 * Найти все элементы по заданному условию, поиск осуществляется в глубину, не учитывает корневой элемент поиска
 * @param before если указан данный параметр, то будут найдены все элементы удовлетворяющие условию до этого
 * @param reverse искать в обратном порядке, поиск осуществляется по getChildElements()
 * @param condition условие поиска
 * @return список [BaseElement]
 */
fun BaseContainerElement.findAll(
    before : BaseElement? = null,
    reverse : Boolean = false,
    condition : ((BaseElement) -> Boolean)
) : List<BaseElement> {
    // Нужна для отслеживания before в рекурсии, чтобы в основной функции не добавлять не нужный параметр в возвращаемом значении
    fun BaseContainerElement.innerFindAll(
        before : BaseElement? = null,
        reverse : Boolean = false,
        condition : ((BaseElement) -> Boolean)
    ) : Pair<List<BaseElement>, Boolean> {
        val result = mutableListOf<BaseElement>()
        var isBeforeFind = false
        for (it in if (reverse) getChildElements().reversed() else getChildElements()) {

            // Если найден элемент до которого осуществляем поиск, то прерываем цикл
            if (before != null && it == before) {
                isBeforeFind = true
                break
            }

            // Проверяем условие и добавляем элемент в список
            if (condition.invoke(it)) {
                result.add(it)
            }

            // Для всех контейнеров элементов осуществляем такой же поиск
            if (it is BaseContainerElement) {
                val nestedResults = it.innerFindAll(before, reverse, condition)
                result.addAll(nestedResults.first)

                // Если во вложенном поиске встретили before, прекращаем поиск
                if (nestedResults.second) {
                    isBeforeFind = true
                    break
                }
            }
        }
        return result to isBeforeFind
    }

    return innerFindAll(before, reverse, condition).first
}

/**
 * Для каждого элемента
 * @param before до какого элемента осуществлять перебор
 * @param reverse перебор в обратном порядке, перебор осуществляется по getChildElements()
 * @param block действие для элемента перебора
 */
fun BaseContainerElement.forEach(
    before: BaseElement? = null,
    reverse: Boolean = false,
    block: (BaseElement) -> Unit
) {
    // Нужна для отслеживания before в рекурсии, чтобы в основной функции не добавлять не нужный параметр в возвращаемом значении
    fun BaseContainerElement.innerForEach(
        before : BaseElement? = null,
        reverse : Boolean = false,
        block: (BaseElement) -> Unit
    ) : Boolean {
        var isBeforeFind = false
        for (it in if (reverse) getChildElements().reversed() else getChildElements()) {

            // Если найден элемент до которого осуществляем перебор, то прерываем цикл
            if (before != null && it == before) {
                isBeforeFind = true
                break
            }

            // Действие с элементом
            block.invoke(it)

            // Для каждого дочернего элемента
            if (it is BaseContainerElement) {
                val nestedResults = it.innerForEach(before, reverse, block)

                // Если во вложенном переборе встретили before, прекращаем перебор
                if (nestedResults) {
                    isBeforeFind = true
                    break
                }
            }
        }
        return isBeforeFind
    }

    innerForEach(before, reverse, block)
}

/**
 * Поиск элементов в родителях
 * @param to до какого элемента осуществляем поиск
 * @param condition условие нахождения элемента
 * @return список [BaseElement]
 */
fun BaseElement.findInParents(
    to : BaseElement? = null,
    condition : ((BaseElement) -> Boolean)
) : List<BaseElement> {
    val result = mutableListOf<BaseElement>()
    var currentParent = parent

    // Осуществляем поиск, пока родитель существует и не является to
    while (currentParent != null && currentParent != to) {

        // Для каждого элемента родительского контейнера осуществляем поиск
        if (currentParent is BaseContainerElement) {
            for (element in currentParent.getChildElements()) {

                // Возвращаем результат, если наткнулись на to
                if (element == to) {
                    return result
                }

                // Проверяем условие и добавляем результат
                if (condition.invoke(element)) {
                    result.add(element)
                }
            }
        }
        currentParent = currentParent.parent
    }
    return result
}

/**
 * Найти элементы по связи parent
 * @param to до какого элемента осуществляется поиск
 * @param condition условие для отбора элементов
 */
fun BaseElement.findByParents(
    to : BaseElement? = null,
    condition : ((BaseElement) -> Boolean)
) : List<BaseElement> {
    val result = mutableListOf<BaseElement>()
    var currentParent = parent

    // Осуществляем поиск, пока родитель существует и не является to
    while (currentParent != null && currentParent != to) {

        // Если элемент удовлетворяет условию поиска, то добавляем его
        if (condition.invoke(currentParent)) {
            result.add(currentParent)
        }

        currentParent = currentParent.parent
    }
    return result
}

/**
 * Функция удаления элемента из дерева, элемент удаляет себя из родительского контейнера, путем применения метода delete
 */
fun BaseElement.destroy() {
    (parent as? BaseContainerElement)?.delete(this)
        ?: throw IllegalStateException("destroy element error: ${this::class}")
}

/**
 * Удаление всех элементов удовлетворяющих условию из дерева
 */
fun BaseContainerElement.deleteAll(condition : ((BaseElement) -> Boolean)) {
    this.findAll(condition = condition).forEach {
        it.destroy()
    }
}

/**
 * Клонирование списка [BaseElement] элементов сразу с приведением типов
 */
fun <T> MutableList<T>.cloneElements(): MutableList<T> = map { element ->
    when (element) {
        is BaseElement -> element.cloneWithCast<T>()
        else -> throw TypeCastException(
            "Element ${element.toString()} must inherit from BaseElement to support cloning"
        )
    }
}.toMutableList()

/**
 * Клонирование элемента типа [BaseElement] с приведением типа
 */
fun<T> BaseElement.cloneWithCast() : T = clone() as T
