package org.spectral.client

import org.koin.dsl.module

val module = module {
    /*
     * Singletons
     */
    single { (context: SpectralContext) -> Spectral(context) }

    /*
     * Factories
     */
}