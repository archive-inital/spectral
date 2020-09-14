package org.spectral.client

import org.koin.dsl.module
import org.spectral.client.config.SpectralConfig
import org.spectral.client.gui.AppletManager
import org.spectral.client.gui.Gui
import org.spectral.client.gui.splashscreen.SplashScreenManager

val module = module {
    /*
     * Singletons
     */
    single { (context: SpectralContext) -> Spectral(context) }
    single { SpectralConfig() }
    single { Gui() }
    single { AppletManager() }
    single { SplashScreenManager() }

    /*
     * Factories
     */
}