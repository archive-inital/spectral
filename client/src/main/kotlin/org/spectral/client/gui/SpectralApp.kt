package org.spectral.client.gui

import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.spectral.client.gui.controller.FXFrameController
import org.spectral.client.gui.splashscreen.SplashScreenView
import org.spectral.client.gui.view.FXFrameView
import org.spectral.common.Injectable
import tornadofx.*
import kotlin.reflect.KClass

/**
 * The Spectral JavaFX application.
 */
class SpectralApp : App(FXFrameView::class) {

    private val fxFrameController: FXFrameController by inject()

    init {
        FX.dicontainer = object : DIContainer, Injectable {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(type, null, null)
            }
        }

        setStageIcon(Image("/spectral-app.png"))
        importStylesheet("/style.css")
    }

    override fun start(stage: Stage) {
        super.start(stage)
        find<SplashScreenView>().openModal(StageStyle.UNDECORATED)
        fxFrameController.init()
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return true
    }
}