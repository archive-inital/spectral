package org.spectral.deobfuscator.transform.controlflow

class Block {

    var startIndex = 0

    var endIndex = 0

    var prev: Block? = null

    var next: Block? = null

    val origin: Block
        get() {
        var cur = this
        var last = prev

        while(last != null) {
            cur = last
            last = cur.prev
        }

        return cur
    }

    val branches = mutableListOf<Block>()
}