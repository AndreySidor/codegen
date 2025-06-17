package patterns.code

import ast.BaseContainerElement
import ast.elements.*
import ast.elements.Function
import ast.identifiers.UsageIdentifier
import patterns.*

data class PatternWithFunction(
    val namespaces : MutableList<Namespace>,
    val classes : MutableList<Class>,
    val enums : MutableList<EnumClass>,
    val functions : MutableList<Function>,
    val globalVariables : MutableList<Declaration.Variable>
) : BasePattern<MutableList<GlobalArea>>() {
    override fun generate(): MutableList<GlobalArea> {
        val results = mutableListOf<GlobalArea>()
        val functionsWithIdentifier = functions.map { UsageIdentifier().setIn(it) }

        namespaces.forEach { namespace ->
            functionsWithIdentifier.forEach { function ->
                val globals = globalVariables.toMutableList().cloneElements()
                val cloneFunction = function.cloneWithCast<Function>()
                val global = GlobalArea()
                global.elements.add(namespace)
                globals.forEach { variable ->
                    global.elements.add(variable)
                }
                global.elements.add(cloneFunction)
                results.add(global)
            }
        }

        classes.forEach { classElement ->
            functionsWithIdentifier.forEach { function ->
                val parentClass = classElement.parentClass?.cloneWithCast<Class>()
                val globals = globalVariables.toMutableList().cloneElements()
                val cloneFunction = function.cloneWithCast<Function>()
                val global = GlobalArea()
                parentClass?.let(global.elements::add)
                globals.forEach { variable ->
                    global.elements.add(variable)
                }
                global.elements.add(classElement)
                global.elements.add(cloneFunction)
                results.add(global)
            }
        }

        enums.forEach { enumClass ->
            functionsWithIdentifier.forEach { function ->
                val globals = globalVariables.toMutableList().cloneElements()
                val cloneFunction = function.cloneWithCast<Function>()
                val global = GlobalArea()
                global.elements.add(enumClass)
                globals.forEach { variable ->
                    global.elements.add(variable)
                }
                global.elements.add(cloneFunction)
                results.add(global)
            }
        }

        functionsWithIdentifier.forEach { function ->
            val globals = globalVariables.toMutableList().cloneElements()
            val cloneFunction = function.cloneWithCast<Function>()
            val global = GlobalArea()
            globals.forEach { variable ->
                global.elements.add(variable)
            }
            global.elements.add(cloneFunction)
            results.add(global)
        }

        results.forEach { root ->
            root.updateRelations()
            root.deleteAll { element ->
                element !is Class && element !is Namespace && element !is EnumClass
                        && (element is BaseContainerElement && element.findAll { it is UsageIdentifier }.isEmpty())
            }
        }

        return results
    }
}