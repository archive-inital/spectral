package org.spectral.client

import org.koin.core.inject
import org.spectral.client.config.SpectralConfig
import org.spectral.client.gui.splashscreen.SplashScreen
import org.spectral.client.gui.splashscreen.SplashScreenManager
import org.spectral.client.rs.GamepackDownloader
import org.spectral.client.rs.JavConfig
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import tornadofx.launch
import java.net.URL
import kotlin.system.exitProcess

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
     * The spectral configuration file
     */
    private val config: SpectralConfig by inject()

    /**
     * The splash screen value manager.
     */
    private val splashScreenManager: SplashScreenManager by inject()

    /**
     * The jagex configuration map.
     */
    lateinit var javConfig: JavConfig private set

    /**
     * Starts the spectral client.
     */
    fun start() {
        logger.info("Starting Spectral client...")

        /*
         * Load the spectral configuration file.
         */
        logger.info("Loading Spectral configuration file.")
        config.load()

        /*
         * Launch the splash screen.
         */
        this.launchSplashScreen()
    }

    /**
     * Runs the prestart initialization.
     */
    fun preStart() {
        logger.info("Preparing Pre-Start steps.")

        splashScreenManager.progress = 0.1
        splashScreenManager.status = "Preparing client..."

        /*
         * Download the JAV_CONFIG.
         */
        this.downloadJavConfig()

        /*
         * Download the gamepack to Jar file.
         */
        this.downloadGamepack()
    }

    /**
     * Launches the client splash screen.
     */
    private fun launchSplashScreen() {
        logger.info("Launching splash screen.")
        launch<SplashScreen>()
    }

    /**
     * Downloads the JAV_CONFIG from the url set in the server configuration
     * file located with the name: spectral.yml.
     */
    private fun downloadJavConfig() {
        val jagexUrl = config[SpectralConfig.JAGEX_URL]

        logger.info("Downloading the Jagex configuration from.")

        /*
         * Update the splash screen
         */
        splashScreenManager.progress += 0.1
        splashScreenManager.status = "Downloading Jagex configuration..."

        /*
         * Download and set the JAV_CONFIG
         */
        javConfig = JavConfig(jagexUrl)
        javConfig.download()
    }

    /**
     * Downlaods the Jagex gamepack using the loaded JavConfig.
     */
    private fun downloadGamepack() {
        logger.info("Downloading Jagex gamepack.")

        /*
         * Update the splashscreen
         */
        splashScreenManager.progress += 0.1
        splashScreenManager.status = "Downloading Jagex gamepack..."

        if(!::javConfig.isInitialized) {
            logger.error("Jagex config has not been downloaded. Exiting process.")
            exitProcess(-1)
        }

        /*
         * Download the gamepack and save the Jar file.
         */
        GamepackDownloader.downloadGamepack(this.javConfig)
    }
}