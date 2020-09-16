package org.spectral.matcher

import kotlin.math.max

/**
 * Represents a potential match node.
 *
 * @property from Any
 * @property to Any
 * @constructor
 */
class Match(val from: Any, val to: Any) {

    /**
     * The number of times this match was considered for
     * a potential match.
     */
    var count = 0

    /**
     * Whether this match has been simulated during an method execution.
     */
    var executed = false

    /**
     * The strength of the similarity of this match during its simulation.
     */
    var score = 0

    /**
     * Combines two match objects together
     *
     * @param other Match
     */
    fun merge(other: Match) {
        count += other.count
        executed = executed or other.executed
        score = max(score, other.score)
    }
}