package org.spectral.client

import org.koin.dsl.module
import org.spectral.client.gui.splashscreen.SplashScreenController

val module = module {
    /*
     * Singletons
     */
    single { (context: SpectralContext) -> Spectral(context) }
    single { SplashScreenController() }

    /*
     * Factories
     */
}