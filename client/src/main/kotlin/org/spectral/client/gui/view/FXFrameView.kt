package org.spectral.client.gui.view

import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.text.Font
import org.spectral.client.gui.Gui
import tornadofx.*

class FXFrameView : View() {

    private val gui: Gui by di()

    override val root = vbox(0) {
        borderpane {
            left = hbox(8) {
                    paddingTop = 6.0
                    paddingLeft = 6.0
                    paddingRight = 6.0

                    imageview(Image("/spectral-app.png")) {
                        fitWidth = 18.0
                        fitHeight = 18.0
                    }

                    label("Spectral") {
                        font = Font(13.0)
                        style = "-fx-font-weight: bold;"
                    }
                }

            right = hbox(4) {
                paddingTop = 6.0
                paddingLeft = 6.0
                paddingRight = 6.0

                button("X")
            }
        }

        menubar {
            paddingTop = 8.0
            menu("File")
            menu("Edit")
            menu("Plugins")
            menu("Tools")
        }
    }
}