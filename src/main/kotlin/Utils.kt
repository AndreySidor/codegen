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

/**
 * Выбор среднего элемента в списке
 */
fun<T> List<T>.selectMiddleElement(): T? {
    if (this.isEmpty()) return null
    val index = (this.size + 1) / 2 - 1
    return this[index]
}

/**
 * Читаем файл / файлы
 * @param path путь к файлу / директории
 * @param extensions список расширений файлов, которые нужно читать
 */
fun readFiles(path : String, extensions : List<String>) : List<String> {
    try {
        val input = File(path)
        val result = mutableListOf<String>()

        when {
            input.isDirectory -> {
                // Обходим директорию и поддиректории, ищем файлы и читаем их
                input.walkTopDown().forEach {
                    if (it.isFile && extensions.contains(it.extension)) {
                        input.readText()
                    }
                }
            }
            input.isFile -> {
                // Если файл обладает нужным расширением, то читаем его
                if (extensions.contains(input.extension)) {
                    result.add(input.readText())
                }
            }
        }

        return result
    } catch (e : Exception) {
        e.printStackTrace()
        throw IllegalArgumentException("При чтении данных по пути: $path произошла ошибка, проверьте, что путь существует")
    }
}

/**
 * Запуск exe-файла
 * @param exePath полный путь к exe-файлу
 * @param args список аргументов
 * @param workingDir рабочая директория
 * @return boolean значение успеха и сообщение об ошибке
 */
fun runExe(
    exePath: String,
    args: List<String> = emptyList(),
    workingDir: String? = null
): Pair<Boolean, String> {
    // Создаем команду
    val command = mutableListOf(exePath)
    command.addAll(args)

    // Создаем процесс
    val processBuilder = ProcessBuilder(command).apply {
        workingDir?.let { directory(File(it)) }

        // Объединяем stderr и stdout
        redirectErrorStream(true)
    }

    return try {
        // Запускаем процесс
        val process = processBuilder.start()

        // Читаем выход процесса
        val output = process.inputStream.bufferedReader().use { it.readText() }

        // Смотрим код завершения процесса
        val exitCode = process.waitFor()
        Pair(exitCode == 0, output)
    } catch (e: Exception) {
        Pair(false, "Runtime error: ${e.message}")
    }
}

/**
 * Печать исключения через System.err с разделителями
 */
fun Exception.printError() {
    System.err.println("/".repeat(20))
    System.err.println("Error: $message\nStack trace:\n${stackTrace.joinToString("\n")}")
    System.err.println("/".repeat(20))
}

/**
 * Получить файлы
 * @param path путь к файлу / директории
 * @param extensions расширения файлов (обязательно)
 */
fun getFiles(path : String, extensions : List<String>) : List<File> {
    try {
        val inputFile = File(path)
        val result = mutableListOf<File>()
        when {
            inputFile.isFile -> {
                // Если файл обладает нужным расширением, то возвращаем его
                if (extensions.contains(inputFile.extension)) {
                    result.add(inputFile)
                }
            }
            inputFile.isDirectory -> {
                // Обходим директорию и поддиректории, ищем файлы и возвращаем их
                inputFile.walkTopDown().forEach {
                    if (it.isFile && extensions.contains(it.extension)) {
                        result.add(it)
                    }
                }
            }
        }
        return result
    } catch (e : Exception) {
        e.printStackTrace()
        throw IllegalArgumentException("При чтении данных по пути: $path произошла ошибка, проверьте, что путь существует")
    }
}

/**
 * Json с настройками
 */
val json = Json {
    // Форматирует JSON с отступами и переносами строк для читаемости
    // Пример:
    // {
    //   "key": "value"
    // }
    // вместо {"key":"value"}
    prettyPrint = true

    // Игнорирует неизвестные ключи при десериализации
    ignoreUnknownKeys = true

    // Сериализует все свойства, даже если они имеют значения по умолчанию
    // Если false - свойства со значениями по умолчанию будут пропускаться
    encodeDefaults = true
}