package patterns

abstract class RandomPattern<E, R>(
    val difficult: Difficult? = null,
    val errors : List<E>? = null
) : BasePattern<R>()