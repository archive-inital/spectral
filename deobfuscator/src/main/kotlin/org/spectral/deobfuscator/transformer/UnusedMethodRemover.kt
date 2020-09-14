package org.spectral.deobfuscator.transformer

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodInsnNode
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.util.*
import org.spectral.asm.Class
import org.spectral.deobfuscator.common.Transform

/**
 * Removes methods which are unused in the client.
 */
@Transform(priority = 8)
class UnusedMethodRemover : Transformer {

    override fun transform(pool: ClassPool) {
        val unusedMethods = pool.unusedMethods
        var counter = 0

        pool.classes.forEach { c ->
            val methodIterator = c.methods.iterator()
            while(methodIterator.hasNext()) {
                val m = methodIterator.next()
                val mName = c.name + "." + m.name + m.desc
                if(mName !in unusedMethods) continue
                methodIterator.remove()
                counter++
            }
        }

        Logger.info("Removed $counter unused methods.")
    }

    /**
     * Gets a set of the unused method nodes in a [ClassGroupExt] object.
     */
    private val ClassPool.unusedMethods: TreeSet<String>
        get() {
            val namedGroup = this.classes.associateBy { it.name }

            val supers = MultimapBuilder.hashKeys().arrayListValues().build<Class, String>()
            this.classes.forEach { c ->
                c.interfaces.forEach { i ->
                    supers.put(c, i)
                }
                supers.put(c, c.superName)
            }

            val subs = MultimapBuilder.hashKeys().arrayListValues().build<Class, String>()
            supers.forEach { c, v ->
                if(namedGroup.containsKey(v)) {
                    subs.put(namedGroup.getValue(v), c.name)
                }
            }

            val usedMethods = this.classes.asSequence().flatMap { it.methods.asSequence() }
                    .flatMap { it.instructions.iterator().asSequence() }
                    .mapNotNull { it as? MethodInsnNode }
                    .map { it.owner + "." + it.name + it.desc }
                    .toSet()

            val removedMethods = TreeSet<String>()
            this.classes.forEach { c ->
                c.methods.forEach methodLoop@ { m ->
                    if(isMethodUsed(c, m, usedMethods, supers, subs, namedGroup)) return@methodLoop
                    val mName = c.name + "." + m.name + m.desc
                    removedMethods.add(mName)
                }
            }

            return removedMethods
        }

    /**
     * Check whether a method is invoked anywhere in a class group.
     *
     * @param node ClassNode
     * @param method MethodNode
     * @param usedMethods Set<String>
     * @param supers Multimap<ClassNode, String>
     * @param subs Multimap<ClassNode, String>
     * @param namedGroup Map<String, ClassNode>
     * @return Boolean
     */
    private fun isMethodUsed(
            node: Class,
            method: Method,
            usedMethods: Set<String>,
            supers: Multimap<Class, String>,
            subs: Multimap<Class, String>,
            namedGroup: Map<String, Class>
    ): Boolean {
        if(method.name == "<init>" || method.name == "<clinit>") return true
        val mName = node.name + "." + method.name + method.desc
        if(usedMethods.contains(mName)) return true
        var currSupers: Collection<String> = supers[node]
        while (currSupers.isNotEmpty()) {
            currSupers.forEach { c ->
                if (isJdkMethod(c, method.name, method.desc)) return true
                if (usedMethods.contains(c + "." + method.name + method.desc)) return true
            }
            currSupers = currSupers.filter { namedGroup.containsKey(it) }.flatMap { supers[namedGroup.getValue(it)] }
        }
        var currSubs = subs[node]
        while (currSubs.isNotEmpty()) {
            currSubs.forEach { c ->
                if (usedMethods.contains(c + "." + method.name + method.desc)) return true
            }
            currSubs = currSubs.flatMap { subs[namedGroup.getValue(it)] }
        }
        return false
    }

    /**
     * Checks if the method is a OSRS gamepack method or is apart of the JVM std library.
     *
     * @param internalClassName String
     * @param methodName String
     * @param methodDesc String
     * @return Boolean
     */
    private fun isJdkMethod(internalClassName: String, methodName: String, methodDesc: String): Boolean {
        try {
            var classes = listOf(java.lang.Class.forName(Type.getObjectType(internalClassName).className))
            while (classes.isNotEmpty()) {
                for (c in classes) {
                    if (c.declaredMethods.any { it.name == methodName && Type.getMethodDescriptor(it) == methodDesc }) return true
                }
                classes = classes.flatMap {
                    java.util.ArrayList<java.lang.Class<*>>().apply {
                        addAll(it.interfaces)
                        if (it.superclass != null) add(it.superclass)
                    }
                }
            }
        } catch (e: Exception) {

        }
        return false
    }
}