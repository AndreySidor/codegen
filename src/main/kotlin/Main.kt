import elements.*
import elements.Function
import elements.identifiers.UsageIdentifier
import patterns.scope.support.CyclePattern
import patterns.scope.support.IfPattern
import java.io.File
import java.lang.Exception

fun main(args: Array<String>) {
    val result = generateScopeTaskFindVariables()
    File("C:\\Users\\Andrey\\Desktop\\Новая папка\\1.txt").writeText(result)
}

fun generateScopeTaskFindVariables() : String {
    // TODO generate by patterns
    var prefix = ""
    val code = GlobalArea(
        elements = listOf(
            Function(
                params = listOf(
                    Declaration.Parameter(
                        name = "a",
                        type = Type.CHAR
                    )
                ),
                body = Body(
                    elements = listOf(
                        CyclePattern(true, true).generate(),
                        IfPattern(true, true).generate()
                    )
                )
            )
        )
    )


    val formattedCode = formatCodeWithClangFormat(code.toString())

    if (formattedCode == null) {
        throw Exception("Ошибка при форматировании кода")
    } else {
        return "${
            formattedCode.split("\n").indexOfFirst { it.contains(UsageIdentifier.toString()) } + 1
        }\n$prefix\n$formattedCode"
    }
}
