package ast.elements

import ast.*
import patterns.serializers.ClassSerializer
import patterns.serializers.ElementSerializer
import templates.Templates

/**
 * Элемент может находится внутри класса
 */
interface ClassElement

/**
 * Элемент класса
 * @param name имя класса
 * @param parentClass родительский класс
 * @param privateElements приватные поля список [ClassElement]
 * @param protectedElement защищенные поля список [ClassElement]
 * @param publicElements публичные поля список [ClassElement]
 */
data class Class(
    var name : String = "",
    val privateElements : MutableList<ClassElement> = mutableListOf(),
    val protectedElement : MutableList<ClassElement> = mutableListOf(),
    val publicElements : MutableList<ClassElement> = mutableListOf(),
    var parentClass : Class? = null
) : BaseContainerElement(), MultiLine,
    SpaceElement, BodyElement, ClassElement, WithRandomAutocomplete, Serializable<Class> {

    /**
     * Костыль, чтобы при парсинге шаблона найти потом родительский класс, и установить его в поле parentClass
     *
     * Примечание: НЕ ТРОГАТЬ ЭТУ ПЕРЕМЕННУЮ И НЕ ИНИЦИАЛИЗИРОВАТЬ ЕЕ НИЧЕМ
     */
    var parentClassName : String? = null

    override val serializer: ElementSerializer<Class>
        get() = ClassSerializer

    init {
        autocomplete()
        updateRelations()
    }

    override fun autocomplete() {
        // Имя цикла
        if (name.isEmpty()) {
            name = Templates.classNames.random()
        }
    }

    override fun updateRelations() {
        // Простановка связей parent
        (privateElements + protectedElement + publicElements).forEach {
            (it as? BaseElement)?.parent = this
            (it as? BaseContainerElement)?.updateRelations()
        }

        // Простановка связей родительскому классу
        parentClass?.parent = this
        parentClass?.updateRelations()
    }

    override fun toStringArray(): List<String> = buildList {
        // Имя класса и его родителя
        add("class $name ${parentClass?.let { " : ${it.name}" } ?: ""}")

        // Открывающая фигурная скобка
        add("{")

        // Публичные элементы
        publicElements.takeIf { it.isNotEmpty() }?.let {
            add("public:")
            publicElements.forEach {
                when (it) {
                    is SingleLine -> add(it.toString())
                    is MultiLine -> addAll(it.toStringArray())
                }
            }
        }

        // Защищенные элементы
        protectedElement.takeIf { it.isNotEmpty() }?.let {
            add("protected:")
            protectedElement.forEach {
                when (it) {
                    is SingleLine -> add(it.toString())
                    is MultiLine -> addAll(it.toStringArray())
                }
            }
        }

        // Приватные элементы
        privateElements.takeIf { it.isNotEmpty() }?.let {
            add("private:")
            privateElements.forEach {
                when (it) {
                    is SingleLine -> add(it.toString())
                    is MultiLine -> addAll(it.toStringArray())
                }
            }
        }

        // Закрывающая фигурная скобка
        add("};")
    }
}
