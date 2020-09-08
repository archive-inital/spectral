package org.spectral.deobfuscator.util

import java.math.BigInteger

private val INT_MODULUS = BigInteger.ONE.shiftLeft(Integer.SIZE)

private val LONG_MODULUS = BigInteger.ONE.shiftLeft(java.lang.Long.SIZE)

fun invert(value: Int): Int = value.toBigInteger().modInverse(INT_MODULUS).toInt()

fun invert(value: Long): Long = value.toBigInteger().modInverse(LONG_MODULUS).toLong()

fun invert(number: Number): Number {
    return when(number) {
        is Int -> invert(number)
        is Long -> invert(number)
        else -> throw IllegalArgumentException("Invalid multiplier primitive type.")
    }
}

fun isInvertible(value: Int): Boolean = value and 1 == 1

fun isInvertible(value: Long): Boolean = isInvertible(value.toInt())

fun isInvertible(number: Number): Boolean {
    return when(number) {
        is Int, is Long -> isInvertible(number.toInt())
        else -> throw IllegalStateException("Invalid multiplier primitive type.")
    }
}