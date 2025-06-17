package patterns.code

import ast.BaseContainerElement
import ast.BaseElement
import ast.NamedElement
import ast.elements.*
import ast.elements.Function
import patterns.*
import templates.Templates

object CodeGenerator {

    fun generate(root : GlobalArea) : List<GlobalArea> {
        val rootElements = root.getChildElements()

        val classes = mutableListOf<Class>()
        val namespaces = mutableListOf<Namespace>()
        val enums = mutableListOf<EnumClass>()
        val functions = mutableListOf<Function>()
        val globalVariables = mutableListOf<Declaration.Variable>()

        rootElements.forEach { globalElement ->
            when (globalElement) {
                is Class -> {
                    classes.add(globalElement)
                }
                is Namespace -> {
                    namespaces.add(globalElement)
                }
                is EnumClass -> {
                    enums.add(globalElement)
                }
                is Function -> {
                    functions.add(globalElement)
                }
                is Declaration.Variable -> {
                    globalVariables.add(globalElement)
                }
            }
        }

        classes.removeAll { it.getChildElements().count { it is Declaration.Variable } == 0 }
        namespaces.removeAll { it.getChildElements().isEmpty() }
        enums.removeAll { it.getChildElements().isEmpty() }
        functions.removeAll { !it.isDefinition }

        val results = PatternWithFunction(
            namespaces = namespaces.cloneElements(),
            classes = classes.cloneElements(),
            enums = enums.cloneElements(),
            functions = functions.cloneElements(),
            globalVariables = globalVariables.cloneElements()
        ).generate() + PatternFull(
            global = root.cloneWithCast()
        ).generate()

        return results.map {
            it.filterDeclarationsCount()
            it.repairSameName()
            it.deleteLongDefinitions()
            it.removeEmptyNamedScopes()
            it.updateRelations()
            it
        }.filter { it.toStringArray().count() <= 60 }
    }

    private fun BaseContainerElement.filterDeclarationsCount() {
        fun List<BaseElement>.filterVariables() {
            var counterStatic = 0
            var counterNotStatic = 0
            for (i in 0..<this.count()) {
                if (this[i] is Declaration.Variable) {
                    if ((this[i] as Declaration.Variable).isStatic) {
                        counterStatic++
                        counterNotStatic = 0
                        if (counterStatic > 2) {
                            this[i].destroy()
                        }
                    } else {
                        counterNotStatic++
                        counterStatic = 0
                        if (counterNotStatic > 2) {
                            this[i].destroy()
                        }
                    }
                } else {
                    counterStatic = 0
                    counterNotStatic = 0
                }
            }
        }

        when (this) {
            is EnumClass -> {
                val elements = getChildElements()
                for (i in 0..<elements.count()) {
                    if (i > 3) {
                        elements[i].destroy()
                    }
                }
            }
            is Function -> {
                val paramsClone = params.toList()
                for (i in 0..<paramsClone.count()) {
                    if (i > 3) {
                        paramsClone[i].destroy()
                    }
                }
                body?.filterDeclarationsCount()
            }
            is Class -> {
                publicElements.filterIsInstance<BaseElement>().filterVariables()
                protectedElements.filterIsInstance<BaseElement>().filterVariables()
                privateElements.filterIsInstance<BaseElement>().filterVariables()
            }
            else -> {
                getChildElements().filterVariables()
                getChildElements().forEach {
                    (it as? BaseContainerElement)?.filterDeclarationsCount()
                }
            }
        }
    }

    private fun BaseContainerElement.repairSameName() {
        // Собираем все объявления в текущем контейнере
        val declarations = getChildElements()
            .filterIsInstance<Declaration>()
            .filter { it is NamedElement } as List<NamedElement>

        // Находим дубликаты имен
        val nameCounts = declarations.groupingBy { it.name }.eachCount()
        val duplicates = nameCounts.filter { it.value > 1 }.keys

        // Подготавливаем список для переименования
        val needRename = declarations.filter { it.name in duplicates }.toMutableList()

        // Переименовываем дубликаты
        needRename.forEach { declaration ->
            when (declaration) {
                is Declaration.EnumConstant -> declaration.name = Templates.enumConstantNames.random()
                else -> declaration.name = Templates.variableNames.random()
            }
        }

        // Рекурсивно обрабатываем вложенные контейнеры
        getChildElements()
            .filterIsInstance<BaseContainerElement>()
            .forEach { it.repairSameName() }
    }

    private fun BaseContainerElement.deleteLongDefinitions() {
        val variables = findAll { it is Declaration.Variable }.filterIsInstance<Declaration.Variable>()

        for (variable in variables) {
            if (variable.definition != null && variable.definition!!.count() > 15) {
                variable.isDefinition = false
                variable.definition = null
            }
        }
    }

    private fun BaseContainerElement.removeEmptyNamedScopes() {
        this.deleteAll {
            (it is Class && it.getChildElements().isEmpty())
                    || (it is Namespace && it.getChildElements().isEmpty())
                    || (it is EnumClass && it.getChildElements().isEmpty())
        }
    }
}