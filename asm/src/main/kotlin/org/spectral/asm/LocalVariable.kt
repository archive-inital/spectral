package org.spectral.asm

data class LocalVariable(
        val method: Method,
        val isArg: Boolean,
        val name: String,
        val desc: String,
        val index: Int,
        val startInsn: Int,
        val endInsn: Int,
)