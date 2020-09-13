package org.spectral.client.gui.splashscreen

import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.spectral.common.Injectable
import tornadofx.*
import kotlin.reflect.KClass

/**
 * The spectral splash screen which appears during client initialization.
 *
 * @property root VBox
 */
class SplashScreen : App(SplashScreenView::class, SplashScreenStyles::class) {

    init {
        /*
         * Register our dependency injector.
         */
        FX.dicontainer = object : DIContainer, Injectable {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(type, null, null)
            }
        }

        /*
         * Setup stage and CSS stuff.
         */
        setStageIcon(Image("/spectral.png"))
        importStylesheet("/style.css")
    }

    override fun start(stage: Stage) {
        /*
         * Start the stage undecorated.
         */
        stage.initStyle(StageStyle.UNDECORATED)
        super.start(stage)
    }

    companion object {
        /**
         * Launches the splash screen.
         */
        fun launch() {
            tornadofx.launch<SplashScreen>(arrayOf())
        }
    }
}