package patterns.serializers

import ast.Serializable
import ast.elements.Class
import ast.elements.ClassElement
import patterns.Difficult
import patterns.PatternParser

object ClassSerializer : ElementSerializer<Class> {

    override val key: String
        get() = "class"

    override fun serialize(element: Class): String = buildString {
        append("$key{\"${element.name}\";${
            element.parentClass?.let { 
                "\"${it.name}\""
            } ?: ""
        };(${
            element.publicElements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.protectedElement.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.privateElements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Class {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 5) {
            throw IllegalArgumentException("class must contained 5 parameter2")
        }
        return Class(
            name = PatternParser.fromStringParam(params[0]) ?: "",
            parentClass = null, // Пока что хз, как это сделать
            publicElements = PatternParser.parseList<ClassElement>(params[2], difficult).toMutableList(),
            protectedElement = PatternParser.parseList<ClassElement>(params[3], difficult).toMutableList(),
            privateElements = PatternParser.parseList<ClassElement>(params[4], difficult).toMutableList()
        )
    }
}