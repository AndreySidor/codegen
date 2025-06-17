import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.serialization.encodeToString
import patterns.Difficult
import patterns.NavigatorData
import patterns.classification.Classifier
import patterns.code.CodeEngine
import patterns.template.TemplateEngine
import templates.Templates
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

/**
 * Объект, чтобы найти местоположение jar файла при запуске
 */
object JarPath {
    /**
     * Получить местоположение исполняемого файла сборки jar
     */
    fun get(): String? = try {
        File(JarPath::class.java.protectionDomain.codeSource.location.toURI()).parent
    } catch (e: Exception) {
        null
    }
}

fun main(args: Array<String>) {

}

/**
 * Запуск шаблонизатора
 * @param args аргументы
 */
fun runTemplateEngine(args: Array<String>) {
    try {
        // Проверить наличие необходимых модулей
        val jarDir = JarPath.get() ?: throw IllegalStateException("Not found path to TemplateEngine.jar dir")
        val jarDirPath = Paths.get(jarDir).toAbsolutePath()

        // Путь к exe файлу парсера в ttl и json
        val pathToParser = jarDirPath.toFile().walkTopDown().find {
            it.isFile && it.nameWithoutExtension == "ParseToTTLAndJson" && it.extension == "exe"
        }?.toPath()?.toAbsolutePath() ?: throw IllegalStateException("Not found module ParseToTTLAndJson.exe in TemplateEngine.jar dir and childes")

        // Загрузка данных для автоподстановок
        Templates.preload()

        // Парсинг аргументов
        val argParser = ArgParser("TemplateEngine")

        val input by argParser.argument(
            type = ArgType.String,
            fullName = "input",
            description = "Path to file of template or directory with templates"
        )
        val output by argParser.option(
            type = ArgType.String,
            fullName = "output",
            shortName = "o",
            description = "Path output directory"
        ).default(Paths.get("output").absolutePathString())
        val sDifficult by argParser.option(
            type = ArgType.String,
            fullName = "difficult",
            shortName = "d",
            description = "Difficult of templates: E, M, H"
        )

        argParser.parse(args)

        // Создаем выходную директорию, если ее нет
        val outputPath = try {
            val tmpOutput = Paths.get(output)
            if (!Files.exists(tmpOutput)) {
                Files.createDirectories(tmpOutput)
            }
            tmpOutput.toAbsolutePath()
        } catch (e: Exception) {
            throw IllegalStateException("Ошибка при создании директории для выходных данных: ${e.message}")
        }

        // Путь к выходной директории для результатов парсера ttl и json
        val outputParseData = outputPath.resolve("data").apply {
            Files.createDirectories(this)
        }.toAbsolutePath()

        // Временный файл для сохранения кода и парсинга через ParseToTTLAndJson.exe
        val tmpFile = Files.createTempFile("template_engine_", ".cpp").toFile().apply {
            deleteOnExit()
        }

        // Сложность шаблона
        val difficult = sDifficult?.let { Difficult.by(it) }

        // Чтение шаблонов из файла
        val templates = readFiles(input, listOf("txt"))

        // Кол-во успешно сгенерированных задач
        var success = 0

        // Кол-во ошибок при генерации кода и префиксов
        var errorGenerate = 0

        // Кол-во ошибок при парсинге в ttl и json
        var errorParse = 0

        templates.forEach { template ->
            try {
                val (prefixes, code) = TemplateEngine.generate(template, difficult)

                // Форматирование кода
                val formattedCode = formatCodeWithClangFormat(code.toString()) ?: throw IllegalStateException("Error clang-format")

                // Запись код во временный файл
                tmpFile.writeText(formattedCode)

                // Генерация задачи под каждый префикс
                for (prefix in prefixes) {
                    try {
                        // Расстановка знаний в задаче
                        val knowledge = Classifier.classify(prefix, code)

                        // Если задание не содержит знаний, то не генерируем его
                        if (knowledge.isEmpty()) {
                            continue
                        }

                        // Имя файлов ttl и json
                        val outputFileName = randomString(16)

                        // Парсинг ttl и json
                        val (result, message) = runExe(
                            exePath = pathToParser.absolutePathString(),
                            args = listOf(prefix.toString(), tmpFile.absolutePath, outputParseData.absolutePathString(), outputFileName)
                        )

                        // Если парсинг прошел успешно, то создаем файл навигации
                        if (result) {
                            val navigatorFile = outputPath.resolve("navigator_${outputFileName}.json").toFile().apply {
                                createNewFile()
                            }
                            val navigatorData = NavigatorData.create(
                                outputPath,
                                outputParseData,
                                outputFileName,
                                prefix,
                                formattedCode,
                                knowledge
                            )
                            navigatorFile.writeText(json.encodeToString(navigatorData))
                            success++
                        } else {
                            errorParse++
                            throw RuntimeException(message)
                        }
                    } catch (e : Exception) {
                        e.printError()
                    }
                }
            } catch (e : Exception) {
                errorGenerate++
                e.printError()
            }
        }

        // Печать результатов
        println("Success: $success")
        println("Generate error: $errorGenerate")
        println("Parse error: $errorParse")
    } catch (e : Exception) {
        e.printError()
    }
}

/**
 * Генерация из кода
 * @param args аргументы
 */
fun runCodeEngine(args: Array<String>) {
    try {
        // Проверить наличие необходимых модулей
        val jarDir = JarPath.get() ?: throw IllegalStateException("Not found path to CodeEngine.jar dir")
        val jarDirPath = Paths.get(jarDir).toAbsolutePath()

        // Путь к exe файлу парсера в ttl и json
        val pathToTTLAndJsonParser = jarDirPath.toFile().walkTopDown().find {
            it.isFile && it.nameWithoutExtension == "ParseToTTLAndJson" && it.extension == "exe"
        }?.toPath()?.toAbsolutePath() ?: throw IllegalStateException("Not found module ParseToTTLAndJson.exe in CodeEngine.jar dir and childes")

        // Путь к парсеру кода в шаблон
        val pathToTemplateParser = jarDirPath.toFile().walkTopDown().find {
            it.isFile && it.nameWithoutExtension == "ParseToTemplate" && it.extension == "exe"
        }?.toPath()?.toAbsolutePath() ?: throw IllegalStateException("Not found module ParseToTemplate.exe in CodeEngine.jar dir and childes")

        // Загрузка данных для автоподстановок
        Templates.preload()

        // Парсинг аргументов
        val argParser = ArgParser("CodeEngine")

        val input by argParser.argument(
            type = ArgType.String,
            fullName = "input",
            description = "Path to file of template or directory with codes .cpp"
        )
        val output by argParser.option(
            type = ArgType.String,
            fullName = "output",
            shortName = "o",
            description = "Path output directory"
        ).default(Paths.get("output").absolutePathString())

        argParser.parse(args)

        // Создаем выходную директорию, если ее нет
        val outputPath = try {
            val tmpOutput = Paths.get(output)
            if (!Files.exists(tmpOutput)) {
                Files.createDirectories(tmpOutput)
            }
            tmpOutput.toAbsolutePath()
        } catch (e: Exception) {
            throw IllegalStateException("Ошибка при создании директории для выходных данных: ${e.message}")
        }

        // Путь к выходной директории для результатов парсера ttl и json
        val outputParseData = outputPath.resolve("data").apply {
            Files.createDirectories(this)
        }.toAbsolutePath()

        // Временный файл для сохранения шаблона
        val tmpTemplateFile = Files.createTempFile("code_engine_template_", ".txt").toFile().apply {
            deleteOnExit()
        }

        // Директория расположения временного файла
        val tmpFileDir = Paths.get(tmpTemplateFile.parentFile.absolutePath)

        // Временный файл для сохранения кода и парсинга через ParseToTTLAndJson.exe
        val tmpCodeFile = Files.createTempFile("code_engine_code_", ".cpp").toFile().apply {
            deleteOnExit()
        }

        // Кол-во успешно сгенерированных задач
        var success = 0

        // Кол-во ошибок при генерации кода и префиксов
        var errorGenerate = 0

        // Кол-во ошибок при парсинге в ttl и json
        var errorParse = 0

        // Кол-во ошибок при парсинге кода в шаблон
        var errorParseToTemplate = 0

        // Получаем все файлы cpp по входному пути
        val codes = getFiles(input, listOf("cpp"))

        codes.forEach { file ->
            try {
                // Прасинг cpp файла в шаблон
                val (result, message) = runExe(
                    exePath = pathToTemplateParser.absolutePathString(),
                    args = listOf(file.absolutePath, tmpFileDir.absolutePathString(), tmpTemplateFile.nameWithoutExtension)
                )
                if (result) {
                    try {
                        // Чтение сгенерированного шаблона из tmp файла
                        val template = readFiles(tmpTemplateFile.absolutePath, listOf("txt")).firstOrNull()
                            ?: throw IllegalStateException("Can not read tmp_template file by path: ${tmpTemplateFile.absolutePath}}")

                        // Генерация заданий
                        val generatedValues = CodeEngine.generate(template)

                        generatedValues.forEach { (prefixes, code) ->
                            // Форматирование кода
                            val formattedCode = formatCodeWithClangFormat(code.toString()) ?: throw IllegalStateException("Error clang-format")

                            // Запись код во временный файл
                            tmpCodeFile.writeText(formattedCode)

                            // Генерация задачи под каждый префикс
                            for (prefix in prefixes) {
                                try {
                                    // Расстановка знаний в задаче
                                    val knowledge = Classifier.classify(prefix, code)

                                    // Если задание не содержит знаний, то не генерируем его
                                    if (knowledge.isEmpty()) {
                                        continue
                                    }

                                    // Имя файлов ttl и json
                                    val outputFileName = randomString(16)

                                    // Парсинг ttl и json
                                    val (result, message) = runExe(
                                        exePath = pathToTTLAndJsonParser.absolutePathString(),
                                        args = listOf(prefix.toString(), tmpCodeFile.absolutePath, outputParseData.absolutePathString(), outputFileName)
                                    )

                                    // Если парсинг прошел успешно, то создаем файл навигации
                                    if (result) {
                                        val navigatorFile = outputPath.resolve("navigator_${outputFileName}.json").toFile().apply {
                                            createNewFile()
                                        }
                                        val navigatorData = NavigatorData.create(
                                            outputPath,
                                            outputParseData,
                                            outputFileName,
                                            prefix,
                                            formattedCode,
                                            knowledge
                                        )
                                        navigatorFile.writeText(json.encodeToString(navigatorData))
                                        success++
                                    } else {
                                        errorParse++
                                        throw RuntimeException(message)
                                    }
                                } catch (e : Exception) {
                                    e.printError()
                                }
                            }
                        }
                    } catch (e : Exception) {
                        errorGenerate++
                        e.printError()
                    }
                } else {
                    errorParseToTemplate++
                    throw RuntimeException(message)
                }
            } catch (e : Exception) {
                e.printError()
            }
        }

        // Печать результатов
        println("Success: $success")
        println("Generate error: $errorGenerate")
        println("Parse to ttl and json error: $errorParse")
        println("Parse to template error: $errorParseToTemplate")
    } catch (e : Exception) {
        e.printError()
    }
}
