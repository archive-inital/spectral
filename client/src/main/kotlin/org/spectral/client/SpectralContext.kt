package org.spectral.client

/**
 * A context of information provided by the
 * Spectral command line interface or defaults when starting
 * the client.
 *
 * @property verbose Boolean
 * @constructor
 */
data class SpectralContext(
    val verbose: Boolean
)