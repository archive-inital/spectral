package org.spectral.client.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.spectral.client.Launcher
import org.spectral.client.SpectralContext

class SpectralCommand : CliktCommand(
    name = "Spectral Client",
    help = "An Open-Source Third Party Client"
) {

    private val verbose by option("-v", "--verbose", help = "Enables verbose logging mode.").flag(default = false)

    /**
     * Run the root command.
     */
    override fun run() {
        /*
         * Create the spectral context.
         */
        val context = SpectralContext(
            verbose = verbose
        )

        /*
         * Launch
         */
        Launcher.launch(context)
    }
}