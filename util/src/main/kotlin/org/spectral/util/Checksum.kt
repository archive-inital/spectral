package org.spectral.util

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.experimental.and

/**
 * Utility methods for calculating different checksum values of
 * various input type.
 */
object Checksum {

    /**
     * Gets the MD5 checksum of a file and returns the resulting
     * data as a HEX string.
     *
     * @param file File
     * @return String
     */
    fun md5(file: File): String {
        if (!file.exists()) {
            throw FileNotFoundException("Unable to verify MD5 checksum on a non-existent file.")
        }

        /*
         * Read and grab the bytes of the file.
         */
        val bytes: ByteArray
        Files.newInputStream(Paths.get(file.absolutePath)).use { reader ->
            bytes = reader.readAllBytes()
        }

        /*
         * Calculate the MD5 checksum.
         */
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)

        return md.digest().joinToString("") { "%02x".format(it) }
    }
}