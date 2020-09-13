package org.spectral.client

import org.koin.dsl.module
import org.spectral.client.config.SpectralConfig
import org.spectral.client.gui.AppletManager
import org.spectral.client.gui.Gui

val module = module {
    /*
     * Singletons
     */
    single { (context: SpectralContext) -> Spectral(context) }
    single { SpectralConfig() }
    single { Gui() }
    single { AppletManager() }

    /*
     * Factories
     */
}