package patterns

import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Хранит данные для навигации по файлам задачи ttl и json, которые создал парсер
 * @param ttl относительный путь к ttl файлу
 * @param json относительный путь к json файлу
 */
@Serializable
data class NavigatorData(
    val ttl : String,
    val json : String
) {
    companion object {
        /**
         * Создание экземпляра класса
         * @param outputDir путь к выходной директории
         * @param name имя выходного файла без расширения
         */
        private fun createInstance(outputDir: Path, name: String): NavigatorData {
            return NavigatorData(
                ttl = outputDir.resolve("$name.ttl").normalize().toString(),
                json = outputDir.resolve("$name.json").normalize().toString()
            )
        }

        /**
         * @param base базовая директория, где будет располагаться этот файл навигации
         * @param target целевая директория расположения сгенерированных фалов парсером
         * @param name имя файла без расширения
         */
        fun create(base: Path, target: Path, name: String): NavigatorData {

            // Вычисление относительного пути от файла навигации к сгенерированным файлам
            val relativeDir = base.toAbsolutePath().relativize(target.toAbsolutePath())
            return createInstance(relativeDir, name)
        }
    }
}
