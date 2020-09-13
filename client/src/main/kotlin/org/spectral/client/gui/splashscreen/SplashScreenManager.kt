package org.spectral.client.gui.splashscreen

import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Provides colling observables to the splash screen.
 */
class SplashScreenManager {

    internal val progressObservable = PublishSubject.create<Double>()
    internal val statusObservable = PublishSubject.create<String>()

    /**
     * The progress bar value.
     */
    var progress = 0.0
        set(value) {
            field = value
            progressObservable.onNext(field)
        }

    /**
     * The status text value.
     */
    var status = "Preparing client..."
        set(value) {
            field = value
            statusObservable.onNext(field)
        }

}