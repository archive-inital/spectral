package org.spectral.client

import org.koin.core.inject
import org.spectral.client.gui.splashscreen.SplashScreen
import org.spectral.client.gui.splashscreen.SplashScreenManager
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import tornadofx.launch

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
     * The splash screen value manager.
     */
    private val splashScreenManager: SplashScreenManager by inject()

    /**
     * The jagex configuration map.
     */
    lateinit var javConfig: Map<String, String>

    /**
     * Starts the spectral client.
     */
    fun start() {
        logger.info("Starting Spectral client...")

        this.launchSplashScreen()
    }

    /**
     * Runs the prestart initialization.
     */
    fun preStart() {
        logger.info("Preparing Pre-Start steps.")

        splashScreenManager.progress = 0.1
        splashScreenManager.status = "Preparing client..."
    }

    /**
     * Launches the client splash screen.
     */
    private fun launchSplashScreen() {
        logger.info("Launching splash screen.")
        launch<SplashScreen>()
    }
}