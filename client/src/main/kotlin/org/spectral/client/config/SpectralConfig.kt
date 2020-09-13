package org.spectral.client.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.source.yaml.toYaml
import org.spectral.client.common.Defaults
import org.spectral.common.logger.logger
import org.spectral.util.Platform
import java.nio.file.Files

/**
 * Represents the spectral configuration which holds settings for the
 * client per computer.
 */
class SpectralConfig {

    /**
     * The internal configuration
     */
    private var config = Config { addSpec(Companion) }

    /**
     * The name of the configuration file.
     */
    private val fileName = "spectral.yml"

    /**
     * The [Path] to the [fileName] depending on platform.
     */
    private val filePath = Platform.currentPlatform.dataDir.resolve(Defaults.SPECTRAL_DIR).resolve("config/$fileName")

    /**
     * Loads the configuration file from the [filePath]
     */
    fun load() {
        /*
         * If the configuration file does not exist, save it first.
         */
        if(!Files.exists(filePath)) {
            logger.info("Creating default Spectral configuration file: '${filePath}'.")
            this.save()
        }

        config = Config { addSpec(Companion) }.from.yaml.file(filePath.toFile())
        this.save()
    }

    /**
     * Saves the current [config] object to the [filePath]
     */
    private fun save() {
        config.toYaml.toFile(filePath.toFile())
    }

    companion object : ConfigSpec("spectral") {
        val DEV_MODE by optional(false, "dev_mode")
        val AUTO_UPDATE by optional(true, "auto_update")
        val JAGEX_URL by optional("http://oldschool1.runescape.com/", "jagex_url")
    }
}