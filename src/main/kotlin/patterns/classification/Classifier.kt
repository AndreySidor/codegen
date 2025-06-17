package patterns.classification

import ast.elements.GlobalArea
import ast.identifiers.UsageIdentifier
import patterns.findAll
import patterns.prefix.PrefixGenerator

/**
 * Объект классификации задания по знаниям и ошибкам
 */
object Classifier {

    /**
     * Доступные классификации
     */
    private val classifiers : List<Classification> = listOf(
        FirstClassification,
        SecondClassification,
        ThirdClassification,
        FourthClassification,
        FifthClassification,
        SixthClassification,
        SeventhClassification,
        EighthClassification,
        NinthClassification,
        TenthClassification,
        EleventhClassification,
        TwelfthClassification
    )

    /**
     * Классификация задачи
     * @param prefix префикс задачи
     * @param code код задачи
     * @return список найденных в задаче ошибок или знаний для проверки
     */
    fun classify(prefix : PrefixGenerator.Prefix, code : GlobalArea) : List<ScopeKnowledge> {
        // Поиск идентификатора
        val identifier = (code.findAll { it is UsageIdentifier }.firstOrNull() as? UsageIdentifier)
            ?: throw IllegalStateException("classify: not found identifier")

        // Классификация
        return classifiers.mapNotNull { classification ->
            classification.check(prefix, code, identifier)
        }
    }

}