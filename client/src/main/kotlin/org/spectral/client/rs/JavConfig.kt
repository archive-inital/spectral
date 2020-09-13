package org.spectral.client.rs

import org.spectral.common.logger.logger
import java.net.URL

/**
 * Represents the Jagex JAV_CONFIG loaded and parsed from the .ws webpage.
 *
 * @property url The jagex url to load the config from.
 * @constructor
 */
class JavConfig(val url: URL) {

    private val data = hashMapOf<String, String>()

    /**
     * Gets a given entry from the Jav config data.
     *
     * @param key String
     * @return String
     */
    operator fun get(key: String): String {
        if(!data.containsKey(key)) throw IllegalArgumentException("Key: '$key' not found in the JAV_CONFIG data.")
        return data[key]!!
    }

    /**
     * Downloads and parsed the Jagex JAV_CONFIG from the [url].
     */
    fun download() {
        val lines = url.readText(Charsets.UTF_8).split("\n")

        lines.forEach {
            var line = it

            if(line.startsWith("param=")) {
                line = line.substring(6)
            }

            val index = line.indexOf("=")
            if(index >= 0) {
                data[line.substring(0, index)] = line.substring(index + 1)
            }
        }
    }
}