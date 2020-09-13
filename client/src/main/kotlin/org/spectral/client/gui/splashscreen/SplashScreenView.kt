package org.spectral.client.gui.splashscreen

import javafx.geometry.Pos
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

/**
 * The JavaFX splash screen window view.
 *
 * @property root VBox
 */
class SplashScreenView : View("Spectral") {

    private val controller: SplashScreenController by inject()

    override val root = vbox {
        alignment = Pos.CENTER

        setPrefSize(450.0, 400.0)

        imageview(Image("/spectral.png")) {
            fitWidth = 96.0
            fitHeight = 96.0
        }

        label("S P E C T R A L") {
            paddingTop = 32.0
            font = Font(32.0)
            paddingBottom = 40.0
        }

        progressbar(0.0) {
            prefWidth = 300.0

            controller.progress.subscribe {
                this.progress = it
            }
        }

        label("") {
            paddingTop = 16.0
            font = Font(14.0)

            controller.status.subscribe {
                this.text = it
            }
        }
    }

    init {
    }
}