package org.spectral.matcher.impl

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.matcher.util.CompareUtil

/**
 * Finds potential matches between all the static methods in two
 * different [ClassPool] objects.
 */
class StaticMethodMatcher {

    private val results = LinkedHashMultimap.create<Method, Method>()

    /**
     * All the static methods in a [ClassPool] which are not static field initializers.
     */
    private val ClassPool.staticMethods: List<Method> get() = this.classes.flatMap { it.methods }
        .filter { it.isStatic }
        .filter { !it.isConstructor || !it.isInitializer }

    /**
     * Gets all the potential matches for a static [Method] in another [ClassPool] object.
     *
     * @receiver Method
     * @param pool ClassPool
     * @return List<Method>
     */
    private fun Method.getPotentialMatches(pool: ClassPool): List<Method> {
        return pool.staticMethods.filter { CompareUtil.isPotentialMatch(this, it) }
    }

    /**
     * Calculates the potential matching pairs.
     *
     * @param poolA ClassPool
     * @param poolB ClassPool
     * @return Multimap<Method, Method>
     */
    fun calculateMatches(poolA: ClassPool, poolB: ClassPool): Multimap<Method, Method> {
        poolA.staticMethods.forEach { methodA ->
            results.putAll(methodA, methodA.getPotentialMatches(poolB))
        }

        return results
    }
}