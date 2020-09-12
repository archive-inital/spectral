package org.spectral.common.logger

import org.tinylog.Level
import org.tinylog.configuration.Configuration
import org.tinylog.core.TinylogLoggingProvider
import org.tinylog.format.MessageFormatter
import org.tinylog.provider.ContextProvider
import org.tinylog.provider.LoggingProvider
import java.io.File

/**
 * An implementation of the rolling file logger for the spectral client.
 *
 * This allows me to specify specific directories on different platforms after
 * runtime to store rolling logs in.
 *
 * @property provider TinylogLoggingProvider
 */
class SpectralLoggingProvider : LoggingProvider {

    private var provider: TinylogLoggingProvider = TinylogLoggingProvider()

    /**
     * Reloads the configuration for the rolling file logger
     * and ads a given output file configuration option.
     *
     * @param filePath String
     */
    @Suppress("UNCHECKED_CAST")
    fun reload(filePath: String) {
        provider.shutdown()

        val method = Configuration::class.java.getDeclaredMethod("load")
        method.isAccessible = true

        val config = method.invoke(null) as MutableMap<String, String>

        /*
         * Build the config
         */
        config["writer2"] = "rolling file"
        config["writer2.level"] = "debug"
        config["writer2.format"] = "{date:yyyy-MM-dd HH:mm:ss.SSS|min-size=10} | {level|min-size=4} | {thread|min-size=4} | {message}"
        config["writer2.file"] = filePath + File.separator + "client_{date}_{count}.log"
        config["writer2.latest"] = filePath + File.separator + "latest.log"
        config["writer2.charset"] = "UTF-8"
        config["writer2.buffered"] = "true"
        config["writer2.policies"] = "size: 25mb"
        config["writer2.backups"] = "50"
        config["writer2.provider"] = "spectral"

        val frozen = Configuration::class.java.getDeclaredField("frozen")
        frozen.isAccessible = true
        frozen.set(null, false)

        Configuration.replace(config)

        provider = TinylogLoggingProvider()
    }

    override fun getContextProvider(): ContextProvider {
        return provider.contextProvider
    }

    override fun getMinimumLevel(): Level {
        return provider.minimumLevel
    }

    override fun getMinimumLevel(tag: String?): Level {
        return provider.getMinimumLevel(tag)
    }

    override fun isEnabled(depth: Int, tag: String?, level: Level?): Boolean {
        return provider.isEnabled(depth + 1, tag, level)
    }

    override fun log(
        depth: Int,
        tag: String?,
        level: Level?,
        exception: Throwable?,
        formatter: MessageFormatter?,
        obj: Any?,
        vararg arguments: Any?
    ) {
        provider.log(depth + 1, tag, level, exception, formatter, obj, arguments)
    }

    override fun log(
        loggerClassName: String?,
        tag: String?,
        level: Level?,
        exception: Throwable?,
        formatter: MessageFormatter?,
        obj: Any?,
        vararg arguments: Any?
    ) {
        provider.log(loggerClassName, tag, level, exception, formatter, obj, arguments)
    }

    override fun shutdown() {
        provider.shutdown()
    }
}