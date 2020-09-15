package org.spectral.client.gui.view

import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import org.spectral.client.gui.Gui
import org.spectral.client.gui.controller.FXFrameController
import org.spectral.client.gui.graphic.TitleBarIconFactory
import tornadofx.*
import java.awt.Color

class FXFrameView : View() {

    private val gui: Gui by di()
    private val controller: FXFrameController by inject()

    var titleBar: BorderPane by singleAssign()
        private set

    override val root = vbox(0) {
        borderpane {
            titleBar = this

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

            right = hbox(6) {
                paddingTop = 6.0
                paddingLeft = 6.0
                paddingRight = 6.0

                imageview(TitleBarIconFactory.minimizeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true
                }

                imageview(TitleBarIconFactory.maximizeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true
                }

                imageview(TitleBarIconFactory.closeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true
                }
            }
        }

        menubar {
            paddingTop = 14.0
            menu("File")
            menu("Edit")
            menu("Plugins")
            menu("Tools")
        }
    }

    init {
        controller.init()
    }

    companion object {
        private val ICON_NORMAL = Color(166, 181, 197)
        private val ICON_HOVER = Color(107, 133, 158)
    }
}