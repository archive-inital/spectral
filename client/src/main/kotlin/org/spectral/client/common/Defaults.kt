package org.spectral.client.common

/**
 * Defaults which could be configuration settings options
 * however they are internal to the client's code so we just define them
 * here.
 */
object Defaults {

    /**
     * The root Spectral data folder name.
     *
     * A prefix is appended to this path name depending
     * on the users running platform.
     */
    const val SPECTRAL_DIR = "spectral/"

    /**
     * The default required directory paths.
     */
    internal val DEFAULT_DIRS = arrayOf(
        "plugins/",
        "logs/",
        "bin/",
        "mappings/",
        "config/"
    )
}