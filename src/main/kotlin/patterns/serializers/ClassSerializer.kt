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
            element.parentClassId?.let { 
                "\"${it}\""
            } ?: ""
        };(${
            element.publicElements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.protectedElements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        });(${
            element.privateElements.joinToString(",") {
                (it as? Serializable<*>)?.serialize() ?: throw ClassCastException("$it must be Serializable")
            }
        })${
            element.identifier?.let { 
                ";\"$it\""
            } ?: ""
        }}")
    }

    override fun deserialize(pattern: String, difficult: Difficult?): Class {
        val params = PatternParser.parseParams(pattern)
        if (params.count() != 5 && params.count() != 6) {
            throw IllegalArgumentException("class must contained 5 or 6 parameters")
        }
        return Class(
            name = PatternParser.fromStringParam(params[0]) ?: "",
            parentClass = null,
            publicElements = PatternParser.parseList<ClassElement>(params[2], difficult).toMutableList(),
            protectedElements = PatternParser.parseList<ClassElement>(params[3], difficult).toMutableList(),
            privateElements = PatternParser.parseList<ClassElement>(params[4], difficult).toMutableList()
        ).apply {
            parentClassId = PatternParser.fromStringParam(params[1])
            identifier = if (params.count() == 6) PatternParser.fromStringParam(params[5]) else null
        }
    }
}