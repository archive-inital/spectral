package org.spectral.common.logger

import org.tinylog.Level
import org.tinylog.configuration.Configuration
import org.tinylog.format.AdvancedMessageFormatter

object Logger {

    private const val STACKTRACE_DEPTH = 2

    private val formatter = AdvancedMessageFormatter(Configuration.getLocale(), Configuration.isEscapingEnabled())
    val provider = SpectralLoggingProvider()

    // @formatter:off
    private val MINIMUM_LEVEL_COVERS_DEBUG = isCoveredByMinimumLevel(Level.DEBUG)
    private val MINIMUM_LEVEL_COVERS_INFO  = isCoveredByMinimumLevel(Level.INFO)
    private val MINIMUM_LEVEL_COVERS_WARN  = isCoveredByMinimumLevel(Level.WARN)
    private val MINIMUM_LEVEL_COVERS_ERROR = isCoveredByMinimumLevel(Level.ERROR)

    /**
     * Checks whether log entries at [DEBUG][Level.DEBUG] level will be output.
     *
     * @return `true` if [DEBUG][Level.DEBUG] level is enabled, `false` if disabled
     */
    fun isDebugEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_DEBUG && provider.isEnabled(STACKTRACE_DEPTH, null, Level.DEBUG)
    }

    /**
     * Logs a message at [DEBUG][Level.DEBUG] level.
     *
     * @param message
     * Any object with a meaningful [Any.toString] method
     */
    fun debug(message: Any?) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, null, formatter, message)
        }
    }

    /**
     * Logs a message at [DEBUG][Level.DEBUG] level.
     *
     * @param message
     * Text message to log
     */
    fun debug(message: String) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, null, formatter, message)
        }
    }

    /**
     * Logs a lazy message at [DEBUG][Level.DEBUG] level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    fun debug(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, null, formatter, message())
        }
    }

    /**
     * Logs an exception at [DEBUG][Level.DEBUG] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     */
    fun debug(exception: Throwable) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, exception, formatter, null)
        }
    }

    /**
     * Logs an exception with a custom message at [DEBUG][Level.DEBUG] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Text message to log
     */
    fun debug(exception: Throwable, message: String) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, exception, formatter, message)
        }
    }

    /**
     * Logs an exception with a custom lazy message at [DEBUG][Level.DEBUG] level. The message will be only
     * evaluated if the log entry is really output.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Function that produces the message
     */
    fun debug(exception: Throwable, message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, exception, null, message())
        }
    }

    /**
     * Checks whether log entries at [INFO][Level.INFO] level will be output.
     *
     * @return `true` if [INFO][Level.INFO] level is enabled, `false` if disabled
     */
    fun isInfoEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_INFO && provider.isEnabled(STACKTRACE_DEPTH, null, Level.INFO)
    }

    /**
     * Logs a message at [INFO][Level.INFO] level.
     *
     * @param message
     * Any object with a meaningful [Any.toString] method
     */
    fun info(message: Any?) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, null, formatter, message)
        }
    }

    /**
     * Logs a message at [INFO][Level.INFO] level.
     *
     * @param message
     * Text message to log
     */
    fun info(message: String) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, null, formatter, message)
        }
    }

    /**
     * Logs a lazy message at [INFO][Level.INFO] level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    fun info(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, null, formatter, message)
        }
    }

    /**
     * Logs an exception at [INFO][Level.INFO] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     */
    fun info(exception: Throwable) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, exception, formatter, null)
        }
    }

    /**
     * Logs an exception with a custom message at [INFO][Level.INFO] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Text message to log
     */
    fun info(exception: Throwable, message: String) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, exception, formatter, message)
        }
    }

    /**
     * Logs an exception with a custom lazy message at [INFO][Level.INFO] level. The message will be only
     * evaluated if the log entry is really output.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Function that produces the message
     */
    fun info(exception: Throwable, message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, exception, formatter, message())
        }
    }

    /**
     * Checks whether log entries at [WARN][Level.WARN] level will be output.
     *
     * @return `true` if [WARN][Level.WARN] level is enabled, `false` if disabled
     */
    fun isWarnEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_WARN && provider.isEnabled(STACKTRACE_DEPTH, null, Level.WARN)
    }

    /**
     * Logs a message at [WARN][Level.WARN] level.
     *
     * @param message
     * Any object with a meaningful [Any.toString] method
     */
    fun warn(message: Any?) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, null, formatter, message)
        }
    }

    /**
     * Logs a message at [WARN][Level.WARN] level.
     *
     * @param message
     * Text message to log
     */
    fun warn(message: String) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, null, formatter, message)
        }
    }

    /**
     * Logs a lazy message at [WARN][Level.WARN] level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    fun warn(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, null, formatter, message())
        }
    }

    /**
     * Logs an exception at [WARN][Level.WARN] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     */
    fun warn(exception: Throwable) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, exception, formatter, null)
        }
    }

    /**
     * Logs an exception with a custom message at [WARN][Level.WARN] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Text message to log
     */
    fun warn(exception: Throwable, message: String) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, exception, formatter, message)
        }
    }

    /**
     * Logs an exception with a custom lazy message at [WARN][Level.WARN] level. The message will be only
     * evaluated if the log entry is really output.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Function that produces the message
     */
    fun warn(exception: Throwable, message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, exception, formatter, message())
        }
    }

    /**
     * Checks whether log entries at [ERROR][Level.ERROR] level will be output.
     *
     * @return `true` if [ERROR][Level.ERROR] level is enabled, `false` if disabled
     */
    fun isErrorEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_ERROR && provider.isEnabled(STACKTRACE_DEPTH, null, Level.ERROR)
    }

    /**
     * Logs a message at [ERROR][Level.ERROR] level.
     *
     * @param message
     * Any object with a meaningful [Any.toString] method
     */
    fun error(message: Any?) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, null, formatter, message)
        }
    }

    /**
     * Logs a message at [ERROR][Level.ERROR] level.
     *
     * @param message
     * Text message to log
     */
    fun error(message: String) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, null, formatter, message)
        }
    }

    /**
     * Logs an exception at [ERROR][Level.ERROR] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     */
    fun error(exception: Throwable) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, exception, formatter, null)
        }
    }

    /**
     * Logs an exception with a custom message at [ERROR][Level.ERROR] level.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Text message to log
     */
    fun error(exception: Throwable, message: String) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, exception, formatter, message)
        }
    }

    /**
     * Logs an exception with a custom lazy message at [ERROR][Level.ERROR] level. The message will be only
     * evaluated if the log entry is really output.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Function that produces the message
     */
    fun error(exception: Throwable, message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, exception, formatter, message())
        }
    }

    /**
     * Checks if a given severity level is covered by the logging provider's minimum level.
     *
     * @param level
     * Severity level to check
     * @return `true` if given severity level is covered, otherwise `false`
     */
    private fun isCoveredByMinimumLevel(level: Level): Boolean {
        return provider.getMinimumLevel(null).ordinal <= level.ordinal
    }
}