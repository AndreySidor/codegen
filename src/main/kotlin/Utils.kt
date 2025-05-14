import java.io.File
import kotlin.random.Random

fun randomString(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}

fun formatCodeWithClangFormat(code: String): String? {
    return try {
        val name = randomString(16)

        val tempFile = File.createTempFile(name, ".cpp").apply { writeText(code) }

        val process = ProcessBuilder("clang-format", "-style=WebKit", tempFile.absolutePath)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val formattedCode = process.inputStream.bufferedReader().readText()
        tempFile.delete()

        formattedCode
    } catch (e : Exception) {
        e.printStackTrace()
        null
    }
}