package patterns

abstract class StaticPattern<E, R> : BasePattern<R>() {
    abstract val difficult : Difficult
    abstract val errors : List<E>
}