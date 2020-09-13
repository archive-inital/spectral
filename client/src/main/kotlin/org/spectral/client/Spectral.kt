package org.spectral.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.core.inject
import org.spectral.client.common.Defaults
import org.spectral.client.config.SpectralConfig
import org.spectral.client.gui.splashscreen.SplashScreen
import org.spectral.client.gui.splashscreen.SplashScreenManager
import org.spectral.client.rs.GamepackUtil
import org.spectral.client.rs.JavConfig
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import org.spectral.util.Checksum
import org.spectral.util.Platform
import tornadofx.launch
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
    private lateinit var javConfig: JavConfig private set

    /**
     * Whether there was a revision update since last client
     * launch.
     */
    private var revisionUpdate = false

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

        runBlocking(Dispatchers.Default) {
            async {
                /*
                 * Download the JAV_CONFIG
                 */
                downloadJavConfig()
            }.await()

            async {
                /*
                 * Download the Jagex Gamepack
                 */
                downloadGamepack()
            }.await()

            async {
                /*
                 * Verify MD5
                 */
                checkForRevisionUpdate()
            }.await()
        }
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
    private suspend fun downloadJavConfig() {
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
        GamepackUtil.downloadGamepack(this.javConfig)
    }

    /**
     * Checks the latest Gamepack JAR file checksum and compares it to the one
     * saved in the Spectral configuration file.
     *
     * If they differ, we conclude there was a revision update since the
     * last launch of the client.
     */
    private fun checkForRevisionUpdate() {
        logger.info("Checking gamepack MD5 checksums.")

        splashScreenManager.progress += 0.05
        splashScreenManager.status = "Verifying gamepack MD5 checksum..."

        val gamepackFile = Platform.currentPlatform.dataDir
            .resolve(Defaults.SPECTRAL_DIR)
            .resolve("bin/gamepack-raw.jar")
            .toFile()

        /*
         * The latest gamepack JAR MD5 checksum.
         */
        val latestMD5 = Checksum.md5(gamepackFile)
        val lastMD5 = config[SpectralConfig.RAW_GAMEPACK_CHECKSUM]

        logger.info("Latest gamepack MD5: '${latestMD5}' Last gamepack MD5: '${lastMD5}'")

        if(lastMD5 != latestMD5) {
            revisionUpdate = false
            logger.info("No revision update detected. Continuing with client launch.")
        } else {
            revisionUpdate = true
            logger.info("Detected a client revision update.")
        }
    }
}