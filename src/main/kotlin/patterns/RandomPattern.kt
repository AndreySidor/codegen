package patterns

abstract class RandomPattern<E, R>(
    val difficult: Difficult? = null,
    val errors : List<E> = mutableListOf()
) : BasePattern<R>()