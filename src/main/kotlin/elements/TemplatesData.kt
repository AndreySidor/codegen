package elements

import randomString
import kotlin.random.Random

val statements = listOf(
    "true",
    "false",
    "5 < 10",
    "int a = 0",
    "float test = 1.1"
)

val forStatements = listOf(
    "int i = 0; ${statements.random()}; i++",
    "int j = 10; ${statements.random()}; j--",
    "int i = 10; i >= 0; i--",
    "int j = 0; j < 10; j++",
    "int j = 0; j <= 10; j++"
)

val names = listOf(
    "i", "j", "some", "nothing", "f"
)

val functionNames = listOf(
    "do", "run", "close", "dest", "read"
)

enum class Type(val value : String) {
    CONST_CHAR_POINTER("const char*"),
    CHAR("char"),
    INT("int"),
    FLOAT("float"),
    BOOL("bool");

    companion object {
        fun getRandom() : Type = entries[Random.nextInt(0, entries.size)]
    }
}

fun getValueBy(type : Type) : String = when (type) {
    Type.CONST_CHAR_POINTER -> "\"${randomString(Random.nextInt(4, 10))}\""
    Type.CHAR -> "'${randomString(1)}'"
    Type.INT -> Random.nextInt(-1000, 1000).toString()
    Type.FLOAT -> "%.2f".format(Random.nextDouble(-1000.0, 1000.0)).replace(",", ".")
    Type.BOOL -> Random.nextBoolean().toString()
}