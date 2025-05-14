package patterns.scope.support

import elements.Body
import elements.BodyElement
import elements.If
import elements.identifiers.UsageIdentifier
import patterns.BasePattern
import kotlin.random.Random

class IfPattern(
    private val addUsageIdentifier : Boolean = false,
    private val nesting : Boolean = false
) : BasePattern<If>() {
    override fun generate(): If {
        var usageIdentifierIsAdded = false
        val ifBody = mutableListOf<BodyElement>()
        val elseIfBody = mutableListOf<BodyElement>()
        val elseBody = mutableListOf<BodyElement>()

        if (Random.nextBoolean()) {
            ifBody.add(RandomVariablePattern.generate())
        }
        if (Random.nextBoolean() && nesting && addUsageIdentifier) {
            usageIdentifierIsAdded = Random.nextBoolean()
            ifBody.add(CyclePattern(usageIdentifierIsAdded).generate())
        }
        if (!usageIdentifierIsAdded && addUsageIdentifier && Random.nextBoolean()) {
            usageIdentifierIsAdded = true
            ifBody.add(UsageIdentifier)
        }
        if (Random.nextBoolean()) {
            repeat(Random.nextInt(1, 2)) {
                ifBody.add(RandomVariablePattern.generate())
            }
        }

        if (Random.nextBoolean()) {
            elseIfBody.add(RandomVariablePattern.generate())
        }
        if (!usageIdentifierIsAdded && addUsageIdentifier && Random.nextBoolean()) {
            usageIdentifierIsAdded = true
            elseIfBody.add(UsageIdentifier)
        }
        if (Random.nextBoolean()) {
            elseIfBody.add(RandomVariablePattern.generate())
        }

        if (!usageIdentifierIsAdded && addUsageIdentifier && Random.nextBoolean() && nesting) {
            usageIdentifierIsAdded = true
            elseBody.add(IfPattern(true).generate())
        }
        if (!usageIdentifierIsAdded && addUsageIdentifier) {
            usageIdentifierIsAdded = true
            elseBody.add(UsageIdentifier)
        }

        return If(
            body = if (ifBody.isEmpty()) null else Body(elements = ifBody),
            elseIfDeclarations = if (elseIfBody.isEmpty()) null else listOf(
                If.ElseIf(body = Body(elements = elseIfBody))
            ),
            elseBody = if (elseBody.isEmpty()) null else Body(elements = elseBody)
        )
    }
}