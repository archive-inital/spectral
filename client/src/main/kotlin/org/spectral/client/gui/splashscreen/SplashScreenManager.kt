package org.spectral.client.gui.splashscreen

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty

/**
 * A shared manager which holds property observables for
 * the splash screen.
 */
class SplashScreenManager {

    /**
     * The progress percentage value between 0.0 - 1.0 as a double.
     */
    val progress = SimpleDoubleProperty(0.0)

    /**
     * The splash screen progress status text.
     */
    val status = SimpleStringProperty("")
}