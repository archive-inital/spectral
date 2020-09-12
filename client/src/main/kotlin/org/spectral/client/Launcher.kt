package org.spectral.client

import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import org.spectral.client.command.SpectralCommand
import org.spectral.client.common.Defaults
import org.spectral.common.Injectable
import org.spectral.common.logger.Logger
import org.spectral.util.Platform
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.math.log

/**
 * The primary launcher for starting all client services
 */
object Launcher : Injectable {

    /**
     * The spectral instance in the launcher.
     */
    private lateinit var spectralInstance: Spectral

    /**
     * Main method
     *
     * @param args Array<String>
     */
    @JvmStatic
    fun main(args: Array<String>) = SpectralCommand().main(args)

    /**
     * Launches the spectral client.
     *
     * @param context SpectralContext
     */
    fun launch(context: SpectralContext) {
        /*
         * Check directories.
         */
        this.checkDirectories()

        /*
         * Start Dependency Injector.
         */
        startKoin { modules(module) }

        /*
         * Configure the logger
         */
        this.configureLogger()

        /*
         * Start the spectral instance.
         */

        this.spectralInstance = get { parametersOf(context) }
        this.spectralInstance.start()
    }

    /**
     * Check the directories that are required for the
     * client to run.
     */
    private fun checkDirectories() {
        /*
         * Get the current platform
         */
        val platform = Platform.currentPlatform
        val dataPath = platform.dataDir.resolve(Defaults.SPECTRAL_DIR)

        Defaults.DEFAULT_DIRS.map { dataPath.resolve(it) }.forEach { dir ->
            /*
             * Create any directories which do not exist.
             */
            if(!Files.exists(dir)) {
                Files.createDirectories(dir)
            }
        }
    }

    /**
     * Configures the path for the rolling log file
     * depending on the current platform.
     */
    private fun configureLogger() {
        val logsDir = Platform.currentPlatform.dataDir.resolve(Defaults.SPECTRAL_DIR).resolve("logs")

        /*
         * Verify the logs directory has been created.
         */
        if(!Files.exists(logsDir)) {
            throw FileNotFoundException("Default spectral logs directory not found at: $logsDir")
        }

        Logger.provider.reload(logsDir.toAbsolutePath().toString())

        println(logsDir.toString())
    }
}