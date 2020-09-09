package org.spectral.deobfuscator.transform

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.util.*

/**
 * Removes methods which are unused in the client.
 */
class UnusedMethodRemover : Transformer {

    override fun execute(pool: ClassPool) {
        val unusedMethods = pool.unusedMethods
        var counter = 0

        pool.values.forEach { c ->
            val methodIterator = c.node.methods.iterator()
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
            val namedGroup = this.values.associate { it.name to it.node }

            val supers = MultimapBuilder.hashKeys().arrayListValues().build<ClassNode, String>()
            this.values.forEach { c ->
                c.interfaces.forEach { i ->
                    supers.put(c.node, i.name)
                }
                supers.put(c.node, c.parent!!.name)
            }

            val subs = MultimapBuilder.hashKeys().arrayListValues().build<ClassNode, String>()
            supers.forEach { c, v ->
                if(namedGroup.containsKey(v)) {
                    subs.put(namedGroup.getValue(v), c.name)
                }
            }

            val usedMethods = this.values.asSequence().flatMap { it.methods.asSequence() }
                    .flatMap { it.instructions.iterator().asSequence() }
                    .mapNotNull { it as? MethodInsnNode }
                    .map { it.owner + "." + it.name + it.desc }
                    .toSet()

            val removedMethods = TreeSet<String>()
            this.values.forEach { c ->
                c.node.methods.forEach methodLoop@ { m ->
                    if(isMethodUsed(c.node, m, usedMethods, supers, subs, namedGroup)) return@methodLoop
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
            node: ClassNode,
            method: MethodNode,
            usedMethods: Set<String>,
            supers: Multimap<ClassNode, String>,
            subs: Multimap<ClassNode, String>,
            namedGroup: Map<String, ClassNode>
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
            var classes = listOf(Class.forName(Type.getObjectType(internalClassName).className))
            while (classes.isNotEmpty()) {
                for (c in classes) {
                    if (c.declaredMethods.any { it.name == methodName && Type.getMethodDescriptor(it) == methodDesc }) return true
                }
                classes = classes.flatMap {
                    ArrayList<Class<*>>().apply {
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