package org.spectral.matcher.util

import org.objectweb.asm.Type
import org.spectral.asm.Class
import org.spectral.asm.Field
import org.spectral.asm.Method

/**
 * This object holds helper methods that determine whether two objects
 * are qualified to be matched.
 *
 * This doesnt mean they are matching, but rather nothing imediate says they couldnt be based
 * on outside obvious traits.
 */
object CompareUtil {

    /**
     * Gets whether two [Class] are potential matches.
     *
     * @param a ClassNode
     * @param b ClassNode
     * @return Boolean
     */
    fun isPotentialMatch(a: Class, b: Class): Boolean {
        if(a.pool[a.superName] != null && b.pool[b.superName] != null) {
            if(!isPotentialMatch(a.pool[a.superName]!!, b.pool[b.superName]!!)) return false
        }
        else {
            if(a.superName != b.superName) return false
        }
        if(a.interfaces.size != b.interfaces.size) return false

        return true
    }

    /**
     * Gets whether two [Method] are potential matches.
     *
     * @param a MethodNode
     * @param b MethodNode
     * @return Boolean
     */
    fun isPotentialMatch(a: Method, b: Method): Boolean {
        if(a.isStatic != b.isStatic) return false
        if(!isNameObfuscated(a.name) && !isNameObfuscated(b.name)) {
            if(a.name != b.name) return false
        }
        if(!a.isStatic && !b.isStatic) {
            if(!isPotentialMatch(a.owner, b.owner)) return false
        }
        if(!isPotentiallyEqual(a.desc, b.desc)) return false

        return true
    }

    /**
     * Gets whether two [Field] are potential matches.
     *
     * @param a FieldNode
     * @param b FieldNode
     * @return Boolean
     */
    fun isPotentialMatch(a: Field, b: Field): Boolean {
        if(a.isStatic != b.isStatic) return false
        if(!isPotentiallyEqual(a.type, b.type)) return false

        return true
    }

    /**
     * Gets whether two [MethodNode] descriptor strings are
     * a possible match.
     *
     * @param a String
     * @param b String
     * @return Boolean
     */
    fun isPotentiallyEqual(a: String, b: String): Boolean {
        val typeA = Type.getMethodType(a)
        val typeB = Type.getMethodType(b)

        if(typeA.argumentTypes.size != typeB.argumentTypes.size) return false
        if(!isPotentiallyEqual(typeA.returnType, typeB.returnType)) return false

        for(i in typeA.argumentTypes.indices) {
            if(i >= typeB.argumentTypes.size) break

            val argTypeA = typeA.argumentTypes[i]
            val argTypeB = typeB.argumentTypes[i]

            if(!isPotentiallyEqual(argTypeA, argTypeB)) return false

        }

        return true
    }

    /**
     * Gets whether two [Type] (Type descriptors) are potentially equal.
     *
     * @param a Type
     * @param b Type
     * @return Boolean
     */
    fun isPotentiallyEqual(a: Type, b: Type): Boolean {
        if(a.sort != b.sort) return false
        if(a.isPrimitive || b.isPrimitive) {
            return a == b
        }

        return true
    }

    /**
     * Gets whether a name is an obfuscated name or not.
     *
     * @param name String
     * @return Boolean
     */
    fun isNameObfuscated(name: String): Boolean {
        if(name.length <= 2 || (name.length == 3 && name.startsWith("aa"))) return true
        if(name.startsWith("class") || name.startsWith("method") || name.startsWith("field")) return true
        return false
    }

    /**
     * Whether a class name belongs to the JVM std library.
     *
     * @param name String
     * @return Boolean
     */
    fun isJvmClass(name: String): Boolean = name.startsWith("java/")
}