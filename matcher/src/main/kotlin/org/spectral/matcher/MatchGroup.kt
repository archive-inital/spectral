package org.spectral.matcher

import com.google.common.collect.HashMultimap
import org.spectral.asm.Class
import org.spectral.asm.ClassPool
import org.spectral.asm.Field
import org.spectral.asm.Method

/**
 * Represents a collection of [Match] objects.
 *
 * @property poolA ClassPool
 * @property poolB ClassPool
 * @constructor
 */
class MatchGroup(val poolA: ClassPool, val poolB: ClassPool) {

    /**
     * The matches store in this object.
     */
    var matches = HashMultimap.create<Any, Match>()

    /**
     * Whether the matches in this group have been simulated or not.
     */
    var simulated = false

    /**
     * The similarity score of the method execution simulation
     */
    var simulationScore = 0

    /**
     * The A method that has performed an execution simulation
     */
    var simulatedMethodA: Method? = null

    /**
     * The B method that has performed an execution simulation.
     */
    var simulatedMethodB: Method? = null

    /**
     * Gets or creates a new [Match] object.
     *
     * @param from Any
     * @param to Any
     * @return Match
     */
    fun getOrCreate(from: Any, to: Any): Match {
        matches[from].forEach { match ->
            if(match.to == to) return match
        }

        /*
         * If no match is found, create a new one.
         */
        val match = Match(from, to)
        matches.put(from, match)

        return match
    }

    /**
     * Sets two elements of like types as a match.
     *
     * @param from Any
     * @param to Any
     * @return Match
     */
    fun match(from: Any, to: Any): Match {
        val match = this.getOrCreate(from, to)
        match.count++

        return match
    }

    /**
     * Gets the matches of a [from] node with the most considerations.
     *
     * @param from Any
     * @return Any?
     */
    fun highest(from: Any): Any? {
        var highest: Match? = null

        /*
         * Loop through all the matches.
         * Find the one with the highest count of considerations.
         */
        matches[from].forEach { match ->
            if(highest == null || match.count > highest!!.count) {
                highest = match
            }
            else if(match.count == highest!!.count && from.name!! > highest!!.to.name!!) {
                highest = match
            }
        }

        return if(highest != null) highest!!.to else null
    }

    /**
     * Combines two [MatchGroup] collections into a single collection.
     *
     * @param other MatchGroup
     */
    fun merge(other: MatchGroup) {
        other.matches.entries().forEach { entry ->
            val from = entry.key
            val match = entry.value
            val merged = this.getOrCreate(from, match.to)
            merged.merge(match)
        }
    }

    /**
     * Operator alias of the 'highest()' method.
     *
     * @param from Any
     * @return Any?
     */
    operator fun get(from: Any): Any? = highest(from)

    /**
     * Gets all the matches as a flattened 2D map collection.
     *
     * @return Map<Any, Any?>
     */
    fun asMap(): Map<Any, Any?> {
        val map = hashMapOf<Any, Any?>()

        matches.keySet().forEach { from ->
            map[from] = highest(from)
        }

        return map
    }

    /**
     * Gets a collection of raw [Match] objects from a [from] node.
     *
     * @param from Any
     * @return Collection<Match>
     */
    fun asCollection(from: Any): Collection<Match> {
        return matches[from]
    }

    /**
     * Reduces out any matches which have more than one considerations. Only keeps
     * the highest calculated scored matches for each entry.
     */
    fun reduce() {
        /*
         * The comparator for calculating which match consideration to keep.
         */
        val comparator = compareByDescending<Match> { it.score }.thenByDescending { it.count }.thenByDescending { it.name }

        val sortedMatches = matches.values().sortedWith(comparator)

        /*
         * The reduced collection map of [Match] objects.
         */
        val reduced = HashMultimap.create<Any, Match>()

        /*
         * A reverse map of what matches have been reduced out.
         */
        val reversed = hashMapOf<Any, Any>()

        sortedMatches.forEach { match ->
            if(reduced.containsKey(match.from)) {
                return@forEach
            }

            if(reversed.containsKey(match.to)) {
                return@forEach
            }

            reduced.put(match.from, match)
            reversed[match.to] = match.from
        }

        matches = reduced
    }

    /**
     * Gets the name of a source node given it is either of type
     * [Class], [Method], or [Field]
     */
    private val Any.name: String? get() {
        when(this) {
            is Class -> return this.name
            is Method -> return this.owner.name + "." + this.name
            is Field -> return this.owner.name + "." + this.name
        }

        return null
    }
}