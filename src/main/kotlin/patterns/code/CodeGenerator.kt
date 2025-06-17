package patterns.code

import ast.BaseContainerElement
import ast.NamedElement
import ast.elements.*
import ast.elements.Function
import patterns.cloneElements
import patterns.destroy
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
        ).generate()

        return results.map {
            it.filterDeclarationsCount()
            it.repairSameName()
            it.updateRelations()
            it
        }
    }

    private fun BaseContainerElement.filterDeclarationsCount() {
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
            !is Class -> {
                var counter = 0
                val elements = getChildElements()
                for (i in 0..<elements.count()) {
                    if (elements[i] is Declaration.Variable && !(elements[i] as Declaration.Variable).isStatic) {
                        counter++
                        if (counter > 2) {
                            elements[i].destroy()
                        }
                    } else {
                        counter = 0
                    }
                }
                getChildElements().forEach {
                    (it as? BaseContainerElement)?.filterDeclarationsCount()
                }
            }
        }
    }

    private fun BaseContainerElement.repairSameName() {
        // 1. Собираем все объявления в текущем контейнере
        val declarations = getChildElements()
            .filterIsInstance<Declaration>()
            .filter { it is NamedElement } as List<NamedElement>

        // 2. Находим дубликаты имен
        val nameCounts = declarations.groupingBy { it.name }.eachCount()
        val duplicates = nameCounts.filter { it.value > 1 }.keys

        // 3. Подготавливаем список для переименования
        val needRename = declarations.filter { it.name in duplicates }.toMutableList()

        // 4. Переименовываем дубликаты
        needRename.forEach { declaration ->
            when (declaration) {
                is Declaration.EnumConstant -> declaration.name = Templates.enumConstantNames.random()
                else -> declaration.name = Templates.variableNames.random()
            }
        }

        // 5. Рекурсивно обрабатываем вложенные контейнеры
        getChildElements()
            .filterIsInstance<BaseContainerElement>()
            .forEach { it.repairSameName() }
    }
}