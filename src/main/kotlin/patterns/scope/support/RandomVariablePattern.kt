package patterns.scope.support

import elements.Declaration
import patterns.BasePattern
import kotlin.random.Random

object RandomVariablePattern : BasePattern<Declaration.Variable>() {
    override fun generate(): Declaration.Variable {
        val isStatic = Random.nextBoolean()
        val isConst = Random.nextBoolean()
        val isDefinition = Random.nextBoolean()
        return Declaration.Variable(
            isStatic = isStatic,
            isConst = isConst,
            isDefinition = (isConst && !isStatic) || isDefinition
        )
    }
}