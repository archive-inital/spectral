package org.spectral.client.rs

import org.spectral.client.common.Defaults
import org.spectral.common.logger.logger
import org.spectral.util.Platform
import java.net.URL
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * Utility for downloading the RAW Jagex gampack to a Jar file.
 */
object GamepackDownloader {

    /**
     * Downloads the Jagex gampack from the jav config
     *
     * @param javConfig JavConfig
     */
    fun downloadGamepack(javConfig: JavConfig) {
        val gamepackUrl = URL(javConfig.url + "gamepack.jar")

        val gamepackFilePath = Platform.currentPlatform.dataDir
            .resolve(Defaults.SPECTRAL_DIR)
            .resolve("bin/gamepack-raw.jar")

        /*
         * If the gamepack file already exists, delete it.
         */
        if(Files.exists(gamepackFilePath)) {
            try {
                Files.deleteIfExists(gamepackFilePath)
            } catch(e : Exception) {
                logger.error(e) { "Unable to download the Jagex gamepack." }
                exitProcess(-1)
            }
        }

        /*
         * Download the Jagex gamepack.
         */

        val bytes = gamepackUrl.openConnection().getInputStream().readAllBytes()

        /*
         * Write the bytes to the [gamepackFilePath]
         */
        Files.newOutputStream(gamepackFilePath).use { writer ->
            writer.write(bytes)
        }
    }
}