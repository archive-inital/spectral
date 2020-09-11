package org.spectral.asm

class ClassPool {

    private val classMap = hashMapOf<String, Class>()

    val classes: List<Class> get() = classMap.values.toList()

    fun add(entry: Class) {
        if(!classes.contains(entry)) {
            classMap[entry.name] = entry
        }
    }

    fun remove(entry: Class) {
        classMap.remove(entry.name)
    }

    operator fun get(name: String): Class? = classMap[name]


}