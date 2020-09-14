package org.spectral.deobfuscator.common

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Transform(val priority: Int)