package patterns.scope.support

import elements.*
import elements.identifiers.UsageIdentifier
import patterns.BasePattern
import patterns.RandomPattern
import patterns.scope.ScopeErrors
import kotlin.random.Random

class CyclePattern(
    private val addUsageIdentifier : Boolean = false,
    private val nesting : Boolean = false
) : BasePattern<Cycle>() {
    override fun generate(): Cycle {
        val type = if (Random.nextBoolean()) Cycle.Companion.Type.FOR else Cycle.Companion.Type.WHILE
        var condition : String? = null
        val body = mutableListOf<BodyElement>()

        var addOverlapping = Random.nextBoolean()
        var usageIdentifierIsAdded = false

        repeat(Random.nextInt(0, 2)) {
            body.add(RandomVariablePattern.generate())
        }

        if (addOverlapping && type == Cycle.Companion.Type.FOR) {
            val variable : Declaration.Variable = Declaration.Variable(
                type = Type.INT,
                isDefinition = true
            )
            condition = "$variable${
                if (variable.definition!!.toInt() > 0) {
                    " ${variable.name} > 0; ${variable.name}--"
                } else if (variable.definition!!.toInt() < 0) {
                    " ${variable.name} < 0; ${variable.name}++"
                } else {
                    " ${variable.name} < ${Random.nextInt(5, 10)}; ${variable.name}++"
                }
            }"

            body.add(
                If(
                    stmt = Declaration.Variable(
                        name = variable.name,
                        isConst = Random.nextBoolean(),
                        isDefinition = Random.nextBoolean()
                    ).toString().dropLast(1),
                    body = if (Random.nextBoolean()) {
                        usageIdentifierIsAdded = true
                        Body(
                            elements = listOf(
                                UsageIdentifier
                            )
                        )
                    } else null,
                    elseBody = if (!usageIdentifierIsAdded) {
                        usageIdentifierIsAdded = true
                        Body(
                            elements = listOf(
                                UsageIdentifier
                            )
                        )
                    } else null
                )
            )
        }

        if (Random.nextBoolean() && addUsageIdentifier && !usageIdentifierIsAdded && nesting) {
            usageIdentifierIsAdded = Random.nextBoolean()
            body.add(IfPattern(usageIdentifierIsAdded).generate())
        } else if (Random.nextBoolean() && addUsageIdentifier && !usageIdentifierIsAdded && nesting) {
            usageIdentifierIsAdded = Random.nextBoolean()
            body.add(CyclePattern(usageIdentifierIsAdded).generate())
        }

        if (addUsageIdentifier && !usageIdentifierIsAdded) {
            body.add(UsageIdentifier)
        }

        repeat(Random.nextInt(0, 2)) {
            body.add(RandomVariablePattern.generate())
        }

        return Cycle(
            type = type,
            stmt = condition,
            body = Body(
                elements = body
            )
        )
    }
}