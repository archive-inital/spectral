package org.spectral.client.rs

import java.net.URL

/**
 * Represents the Jagex JAV_CONFIG loaded and parsed from the .ws webpage.
 *
 * @property url The jagex url to load the config from.
 * @constructor
 */
class JavConfig(val url: String) {

    private val data = hashMapOf<String, String>()

    /**
     * Gets a given entry from the Jav config data.
     *
     * @param key String
     * @return String
     */
    operator fun get(key: String): String? {
        return data[key]
    }

    /**
     * Downloads and parsed the Jagex JAV_CONFIG from the [url].
     */
    fun download() {
        val lines = URL(url + "jav_config.ws").readText().split("\n")

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