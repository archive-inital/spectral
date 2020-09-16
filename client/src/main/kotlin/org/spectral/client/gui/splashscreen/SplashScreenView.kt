package org.spectral.client.gui.splashscreen

import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.text.Font
import org.spectral.client.Spectral
import org.spectral.client.gui.Gui
import tornadofx.*
import kotlin.concurrent.thread

/**
 * The JavaFX splash screen window view.
 *
 * @property root VBox
 */
class SplashScreenView : View("Spectral") {

    private val gui: Gui by di()
    private val spectral: Spectral by di()
    private val splashScreenManager: SplashScreenManager by di()

    override val root = vbox {
        style = "-fx-border-color: derive(-black-dark, -25%);" +
                "-fx-border-width: 3px;" +
                "-fx-border-style: solid;"

        alignment = Pos.CENTER

        setPrefSize(425.0, 375.0)

        imageview(Image("/spectral.png")) {
            fitWidth = 96.0
            fitHeight = 96.0
        }

        label("S P E C T R A L") {
            paddingTop = 32.0
            font = Font(32.0)
            paddingBottom = 40.0
        }

        progressbar(splashScreenManager.progress) {
            prefWidth = 300.0
        }

        label(splashScreenManager.status) {
            paddingTop = 16.0
            font = Font(14.0)
        }
    }

    override fun onDock() {
        thread {
            spectral.preStart()
            runLater {
                close()
                gui.showFrame()
            }
        }
    }
}