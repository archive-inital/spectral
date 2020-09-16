package org.spectral.client.gui.view

import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import org.spectral.client.Spectral
import org.spectral.client.gui.Gui
import org.spectral.client.gui.controller.FXFrameController
import org.spectral.client.gui.graphic.TitleBarIconFactory
import org.spectral.common.logger.logger
import tornadofx.*
import java.awt.Color
import javax.swing.SwingConstants

class FXFrameView : View() {

    private val controller: FXFrameController by inject()
    private val spectral: Spectral by di()

    var titleBar: BorderPane by singleAssign()

    override val root = vbox(0) {
        style = "-fx-border-color: derive(-black-dark, -25%);" +
                "-fx-border-width: 3px;" +
                "-fx-border-style: solid;"

        borderpane {
            titleBar = this

            left = hbox(8) {
                    paddingTop = 6.0
                    paddingLeft = 8.0
                    paddingRight = 8.0
                    paddingBottom = 12.0

                    imageview(Image("/spectral-app.png")) {
                        fitWidth = 18.0
                        fitHeight = 18.0
                    }

                    menubar {
                        paddingTop = -4.0
                        menu("File") {
                            item("New Tab")
                            item("Close Tab") {
                                isDisable = true
                            }
                            separator()
                            item("Exit").action { spectral.stop() }
                        }

                        menu("Edit") {
                            item("Preferences")
                        }

                        menu("Tools") {
                            item("Developer Tools")
                        }

                        menu("Plugins") {
                            item("Enable/Disable Plugins")
                            item("View Plugins")
                            item("Remove Plugins")
                            separator()
                            item("Discover Plugins")
                        }

                        menu("Help") {
                            item("Join Our Discord")
                            item("Report Issue")
                            item("Knowledge Base")
                            separator()
                            item("Changelog")
                            item("Version")
                            separator()
                            item("Check for Updates")
                        }
                    }

            }

            right = hbox(6) {
                paddingTop = 6.0
                paddingLeft = 8.0
                paddingRight = 8.0
                paddingBottom = 12.0

                /*
                 * Minimize Icon
                 */
                imageview(TitleBarIconFactory.minimizeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true

                    setOnMouseEntered {
                        controller.movable.set(false)
                        image = TitleBarIconFactory.minimizeIcon(16, ICON_HOVER)
                    }

                    setOnMouseExited {
                        controller.movable.set(true)
                        image = TitleBarIconFactory.minimizeIcon(16, ICON_NORMAL)
                    }

                    addEventHandler(MouseEvent.MOUSE_CLICKED) {
                        controller.minimize()
                    }
                }

                /*
                 * Maximize Icon
                 */
                imageview(TitleBarIconFactory.maximizeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true

                    setOnMouseEntered {
                        controller.movable.set(false)
                        image = if(controller.maximized.get()) {
                            TitleBarIconFactory.restoreIcon(16, ICON_HOVER)

                        } else {
                            TitleBarIconFactory.maximizeIcon(16, ICON_HOVER)
                        }
                    }

                    setOnMouseExited {
                        controller.movable.set(true)
                        image = if(controller.maximized.get()) {
                            TitleBarIconFactory.restoreIcon(16, ICON_NORMAL)
                        } else {
                            TitleBarIconFactory.maximizeIcon(16, ICON_NORMAL)
                        }
                    }

                    addEventHandler(MouseEvent.MOUSE_CLICKED) {
                        controller.toggleMaximize()

                        image = if(controller.maximized.get()) {
                            TitleBarIconFactory.restoreIcon(16, ICON_NORMAL)
                        } else {
                            TitleBarIconFactory.maximizeIcon(16, ICON_NORMAL)
                        }
                    }
                }

                imageview(TitleBarIconFactory.closeIcon(16, ICON_NORMAL)) {
                    isPickOnBounds = true

                    setOnMouseEntered {
                        controller.movable.set(false)
                        image = TitleBarIconFactory.closeIcon(16, ICON_HOVER)
                    }

                    setOnMouseExited {
                        controller.movable.set(true)
                        image = TitleBarIconFactory.closeIcon(16, ICON_NORMAL)
                    }

                    addEventHandler(MouseEvent.MOUSE_CLICKED) {
                        spectral.stop()
                    }
                }
            }
        }
    }

    companion object {
        private val ICON_NORMAL = Color(166, 181, 197)
        private val ICON_HOVER = Color(107, 133, 158)
    }
}