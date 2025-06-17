package patterns.code

import ast.BaseContainerElement
import ast.elements.*
import ast.elements.Function
import ast.identifiers.UsageIdentifier
import patterns.*

data class PatternFull(
    val global : GlobalArea
) : BasePattern<List<GlobalArea>>() {

    override fun generate(): List<GlobalArea> {
        val results = mutableListOf<GlobalArea>()
        val functions = global.findAll { it is Function } as List<Function>

        functions.forEach { function ->
            val newFunction = global.findAll { it.id == function.id }.firstOrNull() as? Function
            val newUsageIdentifier = UsageIdentifier()
            if (newFunction != null) {
                newUsageIdentifier.setIn(newFunction)
            }
            val newGlobal = global.cloneWithCast<GlobalArea>()
            newUsageIdentifier.destroy()
            results.add(newGlobal)
        }

        results.forEach { root ->
            root.updateRelations()
            root.deleteAll { element ->
                element !is Class && element !is Namespace && element !is EnumClass
                        && (element is BaseContainerElement && element.findAll { it is UsageIdentifier }.isEmpty())
            }
            val identifier = root.findAll { it is UsageIdentifier }.first() as UsageIdentifier
            root.forEach(identifier, true) {
                if (it is BaseContainerElement && it.parent is GlobalArea && !identifier.isNestedIn(it)) {
                    it.destroy()
                }
            }
        }

        return results
    }

}
