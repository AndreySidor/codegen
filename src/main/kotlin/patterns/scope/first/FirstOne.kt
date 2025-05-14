package patterns.scope.first

import elements.BodyElement
import elements.Declaration
import elements.GlobalArea
import elements.SpaceElement
import patterns.StaticPattern
import patterns.Difficult
import patterns.scope.ResultScope
import patterns.scope.ScopeErrors

object FirstOne : StaticPattern<ScopeErrors, ResultScope>() {
    override val difficult: Difficult
        get() = Difficult.EASY
    override val errors: List<ScopeErrors>
        get() = TODO("Not yet implemented")

    override fun generate(): ResultScope {
        val beforeFunction = mutableListOf<SpaceElement>()
        val functionParams = mutableListOf<Declaration.Parameter>()
        val functionBody = mutableListOf<BodyElement>()
        val afterFunction = mutableListOf<SpaceElement>()
        return TODO()
    }
}