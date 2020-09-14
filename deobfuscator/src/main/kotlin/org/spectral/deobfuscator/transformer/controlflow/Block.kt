package org.spectral.deobfuscator.transformer.controlflow

/**
 * Represents a control flow frame.
 */
class Block {

    /**
     * The start instruction index
     */
    var start: Int = 0

    /**
     * The end instruction index
     */
    var end: Int = 0

    /**
     * The next control flow [Block]
     */
    var next: Block? = null

    /**
     * The previous control flow [Block]
     */
    var prev: Block? = null

    /**
     * The first block in the current block's chain
     */
    val origin: Block get() {
        var cur = this
        var last = prev

        while(last != null) {
            cur = last
            last = cur.prev
        }

        return cur
    }

    /**
     * The conditional branches of this block.
     */
    val branches = mutableListOf<Block>()
}