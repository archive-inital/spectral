package org.spectral.client

import org.koin.core.inject
import org.spectral.client.common.Defaults
import org.spectral.client.config.SpectralConfig
import org.spectral.client.gui.AppletManager
import org.spectral.client.gui.Gui
import org.spectral.client.rs.GamepackUtil
import org.spectral.client.rs.JavConfig
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import org.spectral.util.Checksum
import org.spectral.util.Platform
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
    private val gui: Gui by inject()
    private val appletManager: AppletManager by inject()

    /**
     * The jagex configuration map.
     */
    lateinit var javConfig: JavConfig private set

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

        gui.launch()
    }

    /**
     * Runs the prestart initialization.
     */
    fun preStart() {
        logger.info("Preparing Pre-Start steps.")

        /*
         * Download the Jav config
         */
        this.downloadJavConfig()

        /*
         * Download the Jagex gamepack
         */
        this.downloadGamepack()

        /*
         * Check the gamepack MD5 Checksum for changes since
         * the last launch.
         */
        this.checkGamepackChecksum()

        appletManager.createClient()
        gui.currentApplet = appletManager.applets.first()
    }

    /**
     * Downloads the JAV_CONFIG from the url set in the server configuration
     * file located with the name: spectral.yml.
     */
    private fun downloadJavConfig() {
        val jagexUrl = config[SpectralConfig.JAGEX_URL]

        logger.info("Downloading the Jagex configuration from.")

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
    private fun checkGamepackChecksum() {
        logger.info("Checking gamepack MD5 checksums.")

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

        if(lastMD5 == latestMD5) {
            revisionUpdate = false
            logger.info("No revision update detected. Continuing with client launch.")
        } else {
            revisionUpdate = true
            logger.info("Detected a client revision update.")
        }
    }
}