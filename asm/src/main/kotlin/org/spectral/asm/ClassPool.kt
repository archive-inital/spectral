package org.spectral.asm

import org.spectral.util.collection.asReadOnly
import java.util.function.Consumer

/**
 * Represents a collection of [Class] objects from a common class path source.
 * @constructor Creates an empty [ClassPool].
 */
class ClassPool : Iterable<Class> {

    /**
     * The [Class] objects stored in this pool.
     */
    private val classes = mutableListOf<Class>()
    private val classMap = hashMapOf<String, Class>()

    /**
     * Adds a [Class] to the pool.
     * @param element [Class] to add to the pool.
     */
    fun addClass(element: Class) {
        assert(element.pool == this)

        element.pool = this
        classes.add(element)
        classMap[element.name] = element
    }

    /**
     * Removes a [Class] from the pool.
     * @param element [Class] to remove from the pool.
     */
    fun removeClass(element: Class) {
        classes.remove(element)
        classMap.remove(element.name)
    }

    /**
     * Gets a [List] of [Class]s in the pool inside of a read-only list.
     *
     * In order to modify the class pool classes,
     * @see org.spectral.asm.ClassPool.addClass
     * @see org.spectral.asm.ClassPool.removeClass
     *
     * @return Read-only [List] of [Class]s in the pool.
     */
    fun getClasses(): List<Class> {
        return classes.asReadOnly()
    }

    /**
     * Gets a [Class] from the pool with a given name.
     * Returns null if none is found.
     *
     * @param name String
     * @return Class?
     */
    fun findClass(name: String): Class? {
        return classes.firstOrNull { it.name == name }
    }

    /**
     * Runs an initialization logic for the class pool.
     */
    fun initialize() {
        buildClassGraph()
    }

    /**
     * Rebuilds the class graph for each [Class] in the pool.
     */
    fun buildClassGraph() {
        classes.forEach { it.clearClassGraph() }
        classes.forEach { it.buildClassGraph() }
    }

    /**
     * Gets a [Class] in the pool with a provided class name.
     *
     * If no class is found with the provided name, the method returns null.
     *
     * @param name Class Name
     * @return [Class] with the requested [name]
     */
    operator fun get(name: String): Class? {
        return classMap[name]
    }

    /**
     * Gets a immutable iterator of the class pool.
     * @return A read-only iterator.
     */
    override fun iterator(): Iterator<Class> {
        return classes.iterator().asReadOnly()
    }

    /**
     * Executes an action for each class in the pool.
     * @param action [Consumer] object providing the action.
     */
    override fun forEach(action: Consumer<in Class>) {
        classes.forEach(action)
    }

    /**
     * Executes an action for each class in the pool.
     * @param action [Unit] to execute.
     */
    fun forEach(action: (Class) -> Unit) {
        classes.forEach(action)
    }
}