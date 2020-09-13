package org.spectral.client

import javafx.application.Application.launch
import org.koin.core.inject
import org.spectral.client.gui.splashscreen.SplashScreen
import org.spectral.client.gui.splashscreen.SplashScreenController
import org.spectral.common.Injectable
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
class Spectral(val context: SpectralContext) : Injectable {

    /**
     * The splash screen controller
     */
    private val splashScreenController: SplashScreenController by inject()

    /**
     * Starts the spectral client.
     */
    fun start() {
        logger.info("Initializing...")

        /*
         * Start the splash screen.
         */
        SplashScreen.launch()


    }
}