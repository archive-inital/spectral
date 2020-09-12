package org.spectral.util

import java.nio.file.Path
import java.nio.file.Paths

/**
 * The user machine's running Operating System Platform.
 * Also provides the prefix to the spectral data folder paths.
 *
 * @property dataDir The prefixed path to the data folder depending on the platform.
 * @constructor
 */
enum class Platform(val dataDir: Path) {

    /**
     * Windows Operating System
     */
    WINDOWS(Paths.get(System.getenv("AppData")).resolve("Roaming/")),

    /**
     * Apple / Mac Operating System
     */
    MACOS(Paths.get(System.getProperty("user.home")).resolve("Library/Application Support/")),

    /**
     * Linux Operating System
     */
    LINUX(Paths.get(System.getProperty("user.home"))),

    /**
     * Other operating system
     */
    OTHER(Paths.get(System.getProperty("user.home") ?: "../"));

    companion object {
        /**
         * Gets the current [Platform] of the running operating system.
         */
        val currentPlatform: Platform get() {
            val os = (System.getProperty("os.name")).toUpperCase()

            return if(os.contains("WIN")) {
                WINDOWS
            } else if(os.contains("MAC")) {
                MACOS
            } else if(os.contains("NIX") || os.contains("NUX") || os.contains("AIX")) {
                LINUX
            } else {
                OTHER
            }
        }
    }
}