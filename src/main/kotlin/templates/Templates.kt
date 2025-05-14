package templates

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

object Templates {
    private const val variableNamesPath = "templates_data/variables.json"
    private const val enumConstantNamesPath = "templates_data/constants_enum.json"
    private const val functionNamesPath = "templates_data/functions.json"
    private const val enumNamesPath = "templates_data/enums.json"
    private const val classNamesPath = "templates_data/classes.json"
    private const val namespaceNamesPath = "templates_data/namespaces.json"

    lateinit var variableNames : List<String>
    lateinit var enumConstantNames : List<String>
    lateinit var functionNames : List<String>
    lateinit var enumNames : List<String>
    lateinit var classNames : List<String>
    lateinit var namespaceNames : List<String>

    val statements = listOf(
        "true",
        "false",
        "5 < 10",
        "int a = 0",
        "float test = 1.1"
    )

    val forStatements = listOf(
        "int i = 0; ${statements.random()}; i++",
        "int j = 10; ${statements.random()}; j--",
        "int i = 10; i >= 0; i--",
        "int j = 0; j < 10; j++",
        "int j = 0; j <= 10; j++"
    )

    private val json = Json { ignoreUnknownKeys }
    private const val key = "names"

    fun preload() {
        variableNames = parseStringList(File(variableNamesPath).readText(), key)
        enumConstantNames = parseStringList(File(enumConstantNamesPath).readText(), key)
        functionNames = parseStringList(File(functionNamesPath).readText(), key)
        enumNames = parseStringList(File(enumNamesPath).readText(), key)
        classNames = parseStringList(File(classNamesPath).readText(), key)
        namespaceNames = parseStringList(File(namespaceNamesPath).readText(), key)
    }

    private fun parseStringList(jsonStr: String, fieldName: String): List<String> {
        return json.parseToJsonElement(jsonStr)
            .jsonObject[fieldName]!!
            .jsonArray
            .map { it.jsonPrimitive.content }
    }
}