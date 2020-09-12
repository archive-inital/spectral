package org.spectral.client

import org.spectral.common.logger.logger

/**
 * The Main Spectral Client Object.
 *
 * This class controls everything relating to starting
 * and managing the client.
 *
 * @property context SpectralContext
 * @constructor
 */
class Spectral(val context: SpectralContext) {

    /**
     * Starts the spectral client.
     */
    fun start() {
        logger.info("Initializing...")

    }
}