package org.spectral.client.gui.view

import javafx.scene.text.Font
import tornadofx.*

class FXFrameView : View() {

    override val root = hbox(50) {
        button("hello")

        label("Oh fuck. OSRS in a JavaFX Window :O")  {
            font = Font(24.0)
        }
    }
}