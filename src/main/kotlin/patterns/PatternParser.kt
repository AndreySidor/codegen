package patterns

import ast.BaseElement
import patterns.PatternParser.parse
import patterns.serializers.*
import kotlin.random.Random

/**
 * Объект с методами для парсинга паттерна
 *
 * Примечание:
 *
 * Основной метод [parse] используйте только его
 *
 * Описание
 *
 * Спецсимволы:
 * 1. ! - элемент необходим в паттерне
 * 2. ? - элемент может отсутствовать в паттерне
 * 3. {;} - параметры элемента разделенные точкой с запятой
 * 4. (,) - список с элементами разделенные запятой, только [BaseElement]
 * 5. : - разделитель для уровня сложности, ! или ? и ключевого слова
 * 6. E, M, H - уровни сложности (easy, medium, hard), без учета регистра
 * 7. "" - строковые параметры оборачиваются в двойные кавычки
 * 8. [,] - один из многих, список [BaseElement], который возвращает рандомно один из элементов
 * (МОЖНО ИСПОЛЬЗОВАТЬ ТОЛЬКО ВНУТРИ СПИСКА, НЕ МОЖЕТ БЫТЬ ПУТЫМ И ДОЛЖЕН ВЕРНУТЬ ХОТЯ БЫ ОДИН ЭЛЕМЕНТ)
 *
 * Шаблоны элементов:
 * 1. global{()}
 * 2. namespace{name;()}
 * 3. enum{name;()}
 * 4. class{name;parentClassId;();();()}
 * 5. function{isStatic;isDefinition;type;name;();()}
 * 6. for{stmt;()}
 * 7. while{stmt;()}
 * 8. if{stmt;();();()}
 * 9. elif{stmt;()}
 * 10. variable{isStatic;isConst;isDefinition;type;name;definition}
 * 11. enum_constant{name}
 * 12. parameter{isConst;type;name}
 * 13. body{()}
 *
 * Примечание по использованию шаблона:
 *
 * 1. Прежде, чем засунуть строковый параметр в двойные кавычки, нужно в нем заэкранировать все двойные кавычки
 * 2. Сложность обладает приоритетом над знаками ! и ?, но только в том случае, если в функцию [parse] передали параметр difficult
 * 3. Если не указать сложность, ! или ? знаки, то будет считаться, что элемент необходим обязательно
 * 4. Параметры указываются строго в порядке описанном выше
 * 5. Если какие-либо из параметров отсутствуют, то заменить на пустоту, но количество ; должно совпадать с шаблоном
 * 6. Элементы более низкой сложности включаются в паттерн более высокого уровня сложности
 * 7. Bool значения - true | false
 */
object PatternParser {

    /**
     * Основная функция для парсинга шаблона
     * @param pattern шаблон
     * @param difficult сложность, которую необходимо соблюдать
     * @return элемент AST [BaseElement]
     */
    fun parse(pattern : String, difficult: Difficult?) : BaseElement? {
        // Начальное состояние обработки KEY
        val state = mutableListOf(ParserStates.KEY)

        // Временная переменная, куда записывается последовательность символов
        var tmp = ""

        // Нужно ли рандомизировать нахождение элемента в исходном коде
        var isRandom = false

        // Сложность полученная из шаблона
        var currentDifficult : Difficult? = null

        // Ключ элемента (название)
        var key = ""

        // Параметры элемента в {}
        var params = ""

        val filteredPattern = pattern.trim()

        // Парсинг
        filteredPattern.forEach {
            when (it) {
                ':' -> {
                    // Учитываем только первые символы текущего элемента, далее просто записываем
                    if (state.lastOrNull() == ParserStates.KEY) {
                        // Так как : находится после сложности и знаков ? и !, то дорабатываем только эти данные
                        when (tmp) {
                            "?" -> isRandom = true
                            "!" -> isRandom = false
                            else -> currentDifficult = Difficult.by(tmp.uppercase())
                        }

                        // Очищаем строку для записи нового значения
                        tmp = ""
                    } else {
                        tmp += it
                    }
                }
                '{' -> {
                    // Убираем состояние ключа, работает только для первого элемента, так как разбиваем его
                    if (state.lastOrNull() == ParserStates.KEY) {
                        state.removeLast()

                        // Записываю ключ первого элемента и чищу строку для записи параметра
                        key = tmp
                        tmp = ""
                    }

                    // Если не в строковом параметре, то открывающая скобка добавляет PARAMS в список состояний
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.add(ParserStates.PARAMS)
                    }
                    tmp += it
                }
                '}' -> {
                    // Если не в строковом параметре, то закрывающая скобка удаляет PARAMS из списка состояний
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.removeLast()
                    }
                    tmp += it
                }
                '"' -> {
                    // Если строковый литерал не экранированный, то записываем его в список состояний, либо удаляем соответственно
                    if (tmp.lastOrNull() != '\\') {
                        if (state.lastOrNull() == ParserStates.STRING_PARAM) {
                            state.removeLast()
                        } else {
                            state.add(ParserStates.STRING_PARAM)
                        }
                    }
                    tmp += it
                }
                else -> {
                    // Записываем все остальные символы и пробельные символы, которые находяться внутри строковых параметров
                    if ((it.isWhitespace() && state.lastOrNull() == ParserStates.STRING_PARAM) || !it.isWhitespace()) {
                        tmp += it
                    }
                }
            }
        }

        // Записываю параметр и чищу строку
        params = tmp
        tmp = ""

        // Если список состояний не пуст, значит была ошибка в паттерне
        if (state.isNotEmpty()) {
            throw IllegalStateException("parse: " + state.joinToString(", ") { it.name })
        } else {
            // Создание элемента в зависимости от сложности и рандома
            return when {
                currentDifficult == null || difficult == null ->
                    if (isRandom && !Random.nextBoolean()) null else parseElement(key, params, difficult)
                difficult == Difficult.EASY && currentDifficult != Difficult.EASY -> null
                difficult == Difficult.MEDIUM && currentDifficult == Difficult.HARD -> null
                else -> parseElement(key, params, difficult)
            }
        }
    }

    /**
     * Парсинг конкретного элемента [BaseElement] через его serializer
     * @param key ключ элемента
     * @param params параметры элемента в фигурных скобка
     * @param difficult сложность паттерна
     * @return сгенерированный элемент по ключу key [BaseElement]
     */
    private fun parseElement(
        key : String,
        params : String,
        difficult: Difficult?
    ) : BaseElement {
        return keyToSerializer[key]?.deserialize(params, difficult)
            ?: throw IllegalArgumentException("parseElement: $key")
    }

    /**
     * Парсинг параметров элемента
     * @param params параметры элемента в фигурных скобках
     * @return список параметров в строковом представлении
     */
    fun parseParams(params : String) : List<String> {
        // Список состояний
        val state = mutableListOf<ParserStates>()

        // Переменная для временной записи последовательности символов
        var tmp = ""

        val result = mutableListOf<String>()

        val filteredPattern = params.trim().drop(1).dropLast(1)

        // Парсинг
        filteredPattern.forEach {
            when (it) {
                ';' -> {
                    // Если точка с запятой не в строковом параметре и список состояний пуст, то есть верхний уровень
                    if (state.isEmpty()) {
                        // Добавляем параметр и очищаем временную строку
                        result.add(tmp)
                        tmp = ""
                    } else {
                        tmp += it
                    }
                }
                '(' -> {
                    // Если не в строковом параметре, то ставим состояние списка активным
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.add(ParserStates.LIST)
                    }
                    tmp += it
                }
                ')' -> {
                    // Если не в строковом параметре, то удалаяем установленное состояние списка
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.removeLast()
                    }
                    tmp += it
                }
                '"' -> {
                    // Если строковый литерал не экранированный, то записываем его в список состояний, либо удаляем соответственно
                    if (tmp.lastOrNull() != '\\') {
                        if (state.lastOrNull() == ParserStates.STRING_PARAM) {
                            state.removeLast()
                        } else {
                            state.add(ParserStates.STRING_PARAM)
                        }
                    }
                    tmp += it
                }
                else -> {
                    // Записываем все остальные символы и пробельные символы, которые находяться внутри строковых параметров
                    if ((it.isWhitespace() && state.lastOrNull() == ParserStates.STRING_PARAM) || !it.isWhitespace()) {
                        tmp += it
                    }
                }
            }
        }

        // Записываем последний параметр и чистим список
        result.add(tmp)
        tmp = ""

        // Если список состояний не пуст, значит была ошибка в паттерне
        if (state.isNotEmpty()) {
            throw IllegalStateException("parseParams: " + state.joinToString(", ") { it.name })
        } else {
            return result
        }
    }

    /**
     * Парсинг списка
     * @param list список в строковой форме заключенный в круглые скобки, элементы [BaseElement]
     * @param difficult сложность
     * @return список [T]
     */
    fun<T> parseList(list : String, difficult: Difficult?) : List<T> {
        // Список состояний
        val state = mutableListOf<ParserStates>()

        // Переменная для временной записи последовательности символов
        var tmp = ""

        val result = mutableListOf<T>()

        val oneOfMany = mutableListOf<String>()

        val filteredList = list.trim().drop(1).dropLast(1)

        // Парсинг
        filteredList.forEach {
            when (it) {
                ',' -> {
                    // Если запятая не в строковом параметре и список состояний пуст, то есть верхний уровень
                    if (state.isEmpty()) {
                        // Если список oneOfMany не пустой, то парсим oneOfMany, и очищаем список
                        if (oneOfMany.isNotEmpty()) {
                            result.add(parseOneOfMany(oneOfMany, difficult))
                            oneOfMany.clear()
                        } else {
                            // Парсим элемент, и если он не null, то добавляем в результирующий список
                            parse(tmp, difficult)?.let { element ->
                                (element as? T)?.let(result::add)
                                    ?: throw IllegalArgumentException("parseList: invalid element type")
                            }
                        }
                        tmp = ""

                        // Если 1 ступень и состояние oneOfMany, то добавляем еще одну строку к oneOfMany
                    } else if (state.count() == 1 && state.first() == ParserStates.ONE_OF_MANY) {
                        oneOfMany.add(tmp)
                        tmp = ""
                    } else {
                        tmp += it
                    }
                }
                '{' -> {
                    // Если не в строковом параметре, то ставим состояние параметров активным
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.add(ParserStates.PARAMS)
                    }
                    tmp += it
                }
                '}' -> {
                    // Если не в строковом параметре, то удаляем последнее состояние параметров
                    if (state.lastOrNull() != ParserStates.STRING_PARAM) {
                        state.removeLast()
                    }
                    tmp += it
                }
                '[' -> {
                    // Если список состояний пустой, то добавляем состояние oneOfMany
                    if (state.isEmpty()) {
                        state.add(ParserStates.ONE_OF_MANY)
                    } else {
                        tmp += it
                    }
                }
                ']' -> {
                    // Если список состояний находится на 1 ступени oneOfMany, то удаляем это состояние, и записываем значение tmp в oneOfMany
                    if (state.count() == 1 && state.first() == ParserStates.ONE_OF_MANY) {
                        state.removeLast()
                        if (tmp.isNotEmpty() && tmp.isNotBlank()) {
                            oneOfMany.add(tmp)
                        }
                    } else {
                        tmp += it
                    }
                }
                '"' -> {
                    // Если строковый литерал не экранированный, то записываем его в список состояний, либо удаляем соответственно
                    if (tmp.lastOrNull() != '\\') {
                        if (state.lastOrNull() == ParserStates.STRING_PARAM) {
                            state.removeLast()
                        } else {
                            state.add(ParserStates.STRING_PARAM)
                        }
                    }
                    tmp += it
                }
                else -> {
                    // Записываем все остальные символы и пробельные символы, которые находятся внутри строковых параметров
                    if ((it.isWhitespace() && state.lastOrNull() == ParserStates.STRING_PARAM) || !it.isWhitespace()) {
                        tmp += it
                    }
                }
            }
        }

        // Парсим последний элемент и добавляем в список
        if (tmp.isNotEmpty() && tmp.isNotBlank()) {
            if (oneOfMany.isNotEmpty()) {
                result.add(parseOneOfMany(oneOfMany, difficult))
                oneOfMany.clear()
            } else {
                parse(tmp, difficult)?.let {
                    (it as? T)?.let(result::add)
                        ?: throw IllegalArgumentException("parseList: invalid element type")
                }
            }
        }

        // Если список состояний не пуст, значит была ошибка в паттерне
        if (state.isNotEmpty()) {
            throw IllegalStateException("parseList: " + state.joinToString(", ") { it.name })
        } else {
            return result
        }
    }

    /**
     * Парсинг списка элементов типа один из многих
     * @param many множество элементов в строковой форме [BaseElement]
     * @param difficult сложность шаблона
     * @return элемент [T]
     */
    private fun<T> parseOneOfMany(many : List<String>, difficult: Difficult?) : T {
        val result = mutableListOf<T>()
        many.forEach {
            parse(it, difficult)?.let { element ->
                (element as? T)?.let(result::add)
                    ?: throw IllegalArgumentException("parseOneOfMany: invalid element type")
            }
        }
        if (result.isEmpty()) {
            throw IllegalStateException("parseOneOfMany result must contained at least of one element")
        } else {
            return result.random()
        }
    }

    /**
     * Получение значения из строкового параметра
     * @param param строковый параметр
     * @return строку, либо null, если параметр пустой
     */
    fun fromStringParam(param : String) : String? = param
        .trim()
        .removeSurrounding("\"", "\"") // Удаляем окружающие двойные кавычки
        .replace("\\\"", "\"") // Убираем дополнительную экранизацию с двойных кавычек
        .takeIf { it.isNotEmpty() } // Возвращаем строку, если она не пустая, иначе null

    /**
     * Получение значения из bool параметра
     * @param param строковый параметр
     * @return bool, либо null, если параметр не соответствует строкам true или false
     */
    fun fromBoolParam(param : String) : Boolean? = param
        .trim()
        .lowercase() // Переводим строку в нижний регистр
        .toBooleanStrictOrNull() // Возвращаем либо bool значение, либо null

    /**
     * Карта с ключом в виде ключа элемента и значение - классом сериализатора элемента
     */
    private val keyToSerializer = mapOf(
        ClassSerializer.key to ClassSerializer,
        EnumClassSerializer.key to EnumClassSerializer,
        NamespaceSerializer.key to NamespaceSerializer,
        GlobalAreaSerializer.key to GlobalAreaSerializer,
        FunctionSerializer.key to FunctionSerializer,
        ForSerializer.key to ForSerializer,
        WhileSerializer.key to WhileSerializer,
        IfSerializer.key to IfSerializer,
        ElseIfSerializer.key to ElseIfSerializer,
        VariableSerializer.key to VariableSerializer,
        EnumConstantSerializer.key to EnumConstantSerializer,
        ParameterSerializer.key to ParameterSerializer,
        BodySerializer.key to BodySerializer
    )

    /**
     * Состояния при парсинге
     */
    private enum class ParserStates {
        KEY,
        PARAMS,
        LIST,
        STRING_PARAM,
        ONE_OF_MANY
    }
}