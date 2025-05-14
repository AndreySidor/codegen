package patterns.scope

import elements.GlobalArea

data class ResultScope(
    val globalArea : GlobalArea,
    val prefix : String? = null
)
