import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.random.Random

/**
 * Генерация рандомной строки заданной длинны
 * @param length длинна
 */
fun randomString(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}

/**
 * Форматирование кода с помощью clang-format, стили: LLVM, Google, Chromium, Mozilla, WebKit, Microsoft, GNU
 * @param code код в строковом представлении
 */
fun formatCodeWithClangFormat(code: String): String? {
    return try {
        val name = randomString(16)

        val tempFile = File.createTempFile(name, ".cpp").apply { writeText(code) }

        val process = ProcessBuilder("clang-format", "-style=WebKit", tempFile.absolutePath)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val formattedCode = process.inputStream.bufferedReader().readText()
        tempFile.delete()

        formattedCode
    } catch (e : Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Парсинг json списка со строками
 * @param jsonStr json
 * @param fieldName имя поля
 */
fun parseStringList(jsonStr: String, fieldName: String): List<String> {
    return Json.parseToJsonElement(jsonStr)
        .jsonObject[fieldName]!!
        .jsonArray
        .map { it.jsonPrimitive.content }
}