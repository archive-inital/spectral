package org.spectral.client.gui.splashscreen

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.text.Font
import tornadofx.*

/**
 * The JavaFX splash screen window view.
 *
 * @property root VBox
 */
class SplashScreenView : View("Spectral") {

    private val splashScreenManager: SplashScreenManager by di()

    override val root = vbox {
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

        progressbar(0.0) {
            prefWidth = 300.0

            splashScreenManager.progressObservable.subscribe {
                this.progress = it
            }
        }

        label("") {
            paddingTop = 16.0
            font = Font(14.0)

            splashScreenManager.statusObservable.subscribe {
                this.text = it
            }
        }
    }
}