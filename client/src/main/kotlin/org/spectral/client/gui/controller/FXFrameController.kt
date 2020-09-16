package org.spectral.client.gui.controller

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Rectangle2D
import javafx.scene.input.MouseButton
import javafx.stage.Screen
import org.spectral.client.gui.Gui
import org.spectral.client.gui.view.FXFrameView
import tornadofx.Controller
import tornadofx.onChange
import javax.swing.JFrame

/**
 * Responsible for providing movement logic to the Spectral JavaFX
 * viewport or Frame window.
 *
 * @author Kyle Escobar
 */
class FXFrameController : Controller() {

    private val maximized = SimpleBooleanProperty(false)
    private val resizable = SimpleBooleanProperty(true)
    private val snappable = SimpleBooleanProperty(true)

    private val frame: FXFrameView by inject()
    private val gui: Gui by di()

    private val stage by lazy { gui.frame }
    private val handle by lazy { frame.titleBar }

    /*
     * Internal states
     */
    private var moving = false
    private var resizing = false
    private var prevSizeX = 0
    private var prevSizeY = 0
    private var prevPosX = 0
    private var prevPosY = 0

    /*
     * Stage setter properties
     */

    private var stageX: Int
        get() = stage.x
        set(value) {
            stage.setLocation(value, stage.y)
        }

    private var stageY: Int
        get() = stage.y
        set(value) {
            stage.setLocation(stage.x, value)
        }

    private var stageWidth: Int
        get() = stage.width
        set(value) {
            stage.setSize(value, stage.height)
            gui.fxFrameWrapper.setBounds(0, 0, value + 6, gui.fxFrameWrapper.height)
        }

    private var stageHeight: Int
        get() = stage.height
        set(value) {
            stage.setSize(stage.width, value)
            gui.fxFrameWrapper.setBounds(0, 0, gui.fxFrameWrapper.width, value + 72)
        }

    /**
     * Initializes the FX Frame
     */
    fun init() {
        prevSizeX = stage.width
        prevSizeY = stage.height
        prevPosX = stage.x
        prevPosY = stage.y

        this.initMoveControls()
    }

    /**
     * Initialize and register frame movement events.
     */
    private fun initMoveControls() {
        var dx = 0
        var dy = 0
        var startX = 0
        var startY = 0

        /*
         * Event Listeners
         */
        handle.setOnMousePressed { event ->
            if(event.isPrimaryButtonDown) {
                dx = event.sceneX.toInt()
                dy = event.sceneY.toInt()

                prevSizeX = stage.width
                prevSizeY = stage.height
                prevPosX = stage.x
                prevPosY = stage.y

                startX = event.screenX.toInt()
                startY = handle.prefHeight(stage.height.toDouble()).toInt()
            }
        }

        handle.setOnMouseDragged { event ->
            if(event.isPrimaryButtonDown) {
                moving = true

                stageX = (event.screenX - dx).toInt()
                stageY = (event.screenY - dy).toInt()
            }
        }

        handle.setOnMouseReleased { event ->
            if(event.button == MouseButton.PRIMARY && event.screenX != startX.toDouble()) {
                moving = false
            }
        }
    }
}