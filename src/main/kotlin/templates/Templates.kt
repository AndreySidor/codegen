package templates

import parseStringList
import java.io.File

/**
 * Шаблоны для автозаполнения ключевых полей в элементах AST
 */
object Templates {
    // ------------------------------------ Пути к данным в форматах json ----------------------------------------
    private const val variableNamesPath = "templates_data/variables.json"
    private const val enumConstantNamesPath = "templates_data/constants_enum.json"
    private const val functionNamesPath = "templates_data/functions.json"
    private const val enumNamesPath = "templates_data/enums.json"
    private const val classNamesPath = "templates_data/classes.json"
    private const val namespaceNamesPath = "templates_data/namespaces.json"
    private const val statementsPath = "templates_data/if_statements.json"
    private const val forStatementsPath = "templates_data/for_statements.json"

    // ----------------------------- Списки, куда сохраняются загруженные шаблоны ---------------------------------
    lateinit var variableNames : List<String>
    lateinit var enumConstantNames : List<String>
    lateinit var functionNames : List<String>
    lateinit var enumNames : List<String>
    lateinit var classNames : List<String>
    lateinit var namespaceNames : List<String>
    lateinit var statements : List<String>
    lateinit var forStatements : List<String>

    /**
     * Ключ поля списка в json
     */
    private const val key = "names"

    /**
     * Предзагрузка шаблонных данных для автозаполнения полей элементов из json
     *
     * !!!Примечание!!!
     *
     * Вызывать данную функцию в начале работы генератора, так как без данных автозаполнения,
     * программа будет заверщшаться с ошибкой
     */
    fun preload() {
        variableNames = parseStringList(File(variableNamesPath).readText(), key)
        enumConstantNames = parseStringList(File(enumConstantNamesPath).readText(), key)
        functionNames = parseStringList(File(functionNamesPath).readText(), key)
        enumNames = parseStringList(File(enumNamesPath).readText(), key)
        classNames = parseStringList(File(classNamesPath).readText(), key)
        namespaceNames = parseStringList(File(namespaceNamesPath).readText(), key)
        statements = parseStringList(File(statementsPath).readText(), key)
        forStatements = parseStringList(File(forStatementsPath).readText(), key)
    }
}