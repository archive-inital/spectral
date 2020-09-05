package org.spectral.deobfuscator.transform.controlflow

/**
 * Represents a control flow instruction list block.
 */
class Block {

    /**
     * The starting instruction index of this block
     */
    var start = 0

    /**
     * The ending instruction index of this block
     */
    var end = 0

    /**
     * The block before this object in the root chain.
     */
    var prev: Block? = null

    /**
     * The block after this object in the root chain.
     */
    var next: Block? = null

    /**
     * The first block in the root chain the current block
     * is in.
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
     * The conditional branches in this block.
     */
    val branches = mutableListOf<Block>()
}