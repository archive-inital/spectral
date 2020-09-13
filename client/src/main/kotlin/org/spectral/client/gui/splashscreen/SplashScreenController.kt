package org.spectral.client.gui.splashscreen

import io.reactivex.rxjava3.core.Observable
import tornadofx.Controller

/**
 * Provides colling observables to the splash screen.
 */
class SplashScreenController : Controller() {

    /**
     * The progress bar value.
     */
    val progress = Observable.just(0.0)

    /**
     * The status text value.
     */
    val status = Observable.just("Preparing client...")
}