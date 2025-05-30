package ast.elements

import ast.*
import patterns.cloneElements
import patterns.fullName
import patterns.serializers.ClassSerializer
import patterns.serializers.ElementSerializer
import templates.Templates

/**
 * Элемент может находиться внутри класса
 */
interface ClassElement

/**
 * Элемент класса
 * @param name имя класса
 * @param parentClass родительский класс
 * @param privateElements приватные поля список [ClassElement]
 * @param protectedElements защищенные поля список [ClassElement]
 * @param publicElements публичные поля список [ClassElement]
 */
data class Class(
    override var name : String = "",
    val privateElements : MutableList<ClassElement> = mutableListOf(),
    val protectedElements : MutableList<ClassElement> = mutableListOf(),
    val publicElements : MutableList<ClassElement> = mutableListOf(),
    var parentClass : Class? = null
) : BaseContainerElement(), MultiLine,
    SpaceElement, BodyElement, ClassElement, WithRandomAutocomplete, Serializable<Class>, NamedScope {

    /**
     * Костыль, чтобы при парсинге шаблона найти потом родительский класс, и установить его в поле parentClass
     *
     * Примечание: НЕ ТРОГАТЬ ЭТУ ПЕРЕМЕННУЮ И НЕ ИНИЦИАЛИЗИРОВАТЬ ЕЕ НИЧЕМ
     */
    var parentClassId : String? = null

    /**
     * Костыль, чтобы при парсинге шаблона нашелся родительский класс, это идентификатор чисто для шаблона
     *
     * Примечание: НЕ ТРОГАТЬ ЭТУ ПЕРЕМЕННУЮ И НЕ ИНИЦИАЛИЗИРОВАТЬ ЕЕ НИЧЕМ
     */
    var identifier : String? = null

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
        (privateElements + protectedElements + publicElements).forEach {
            (it as? BaseElement)?.parent = this
            (it as? BaseContainerElement)?.updateRelations()
        }
    }

    override fun getChildElements(): List<BaseElement> = (publicElements + protectedElements + privateElements).toList() as List<BaseElement>

    override fun delete(element: BaseElement) {
        (element as? ClassElement)?.let {
            publicElements.remove(it)
            protectedElements.remove(it)
            privateElements.remove(it)
        } ?: ClassCastException("class delete: ${element::class}")
    }

    override fun clone(): BaseElement = this.copy(
        parentClass = parentClass,
        publicElements = publicElements.cloneElements(),
        protectedElements = protectedElements.cloneElements(),
        privateElements = privateElements.cloneElements()
    ).apply { updateRelations() }

    override fun toStringArray(): List<String> = buildList {
        // Имя класса и его родителя
        add("class $name ${
            (parentClass as? NamedElement)?.fullName()?.takeIf { it.isNotEmpty() }?.let { 
                ": $it"
            } ?: ""
        }")

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
        protectedElements.takeIf { it.isNotEmpty() }?.let {
            add("protected:")
            protectedElements.forEach {
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
