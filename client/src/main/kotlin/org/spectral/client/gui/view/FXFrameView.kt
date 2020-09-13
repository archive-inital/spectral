package org.spectral.client.gui.view

import javafx.scene.image.Image
import javafx.scene.layout.Region
import javafx.scene.text.Font
import org.spectral.client.gui.Gui
import tornadofx.*

class FXFrameView : View() {

    private val gui: Gui by di()

    override val root = vbox(50) {
        paddingAll = 6.0
        hbox(8) {
            imageview(Image("/spectral-app.png")) {
                fitWidth = 18.0
                fitHeight = 18.0
            }

            label("Spectral") {
                font = Font(13.0)
                style = "-fx-font-weight: bold;"
            }
        }
    }
}