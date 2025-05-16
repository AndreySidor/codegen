package templates

import randomString
import kotlin.random.Random

enum class Type(val value : String) {
    CONST_CHAR_POINTER("const char*"),
    CHAR("char"),
    INT("int"),
    FLOAT("float"),
    BOOL("bool"),
    UNDEFINED("undefined");

    companion object {
        fun getRandom() : Type = entries[Random.nextInt(0, entries.size - 1)]
        fun getBy(value : String) = entries.firstOrNull { it.value == value } ?: UNDEFINED
    }
}

fun getValueBy(type : Type) : String? = when (type) {
    Type.CONST_CHAR_POINTER -> "\"${randomString(Random.nextInt(4, 10))}\""
    Type.CHAR -> "'${randomString(1)}'"
    Type.INT -> Random.nextInt(-1000, 1000).toString()
    Type.FLOAT -> "%.2f".format(Random.nextDouble(-1000.0, 1000.0)).replace(",", ".")
    Type.BOOL -> Random.nextBoolean().toString()
    else -> null
}