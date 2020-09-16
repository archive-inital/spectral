package org.spectral.client.gui.controller

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.MouseButton
import javafx.stage.Screen
import org.spectral.client.gui.Gui
import org.spectral.client.gui.view.FXFrameView
import tornadofx.Controller
import java.awt.Toolkit
import javax.swing.JFrame

/**
 * Responsible for providing movement logic to the Spectral JavaFX
 * viewport or Frame window.
 *
 * @author Kyle Escobar
 */
class FXFrameController : Controller() {

    /**
     * Observable property whether the frame is maximized or not.
     */
    val maximized = SimpleBooleanProperty(false)

    /**
     * Observable property whether the frame is resizable or not.
     * This is always true unless the frame is maximized.
     */
    val resizable = SimpleBooleanProperty(true)

    /**
     * Observable property which determins if the moving events will trigger or not.
     * This is used when you hover over the title bar minimize, maximize, and close icons as we
     * want those button events to take priority.
     */
    val movable = SimpleBooleanProperty(true)

    private val frame: FXFrameView by inject()
    private val gui: Gui by di()
    private val stage by lazy { gui.frame }
    private val handle by lazy { frame.titleBar }

    /**
     * Internal states
     */
    private var moving = false
    private var resizing = false
    private var prevSizeX = 0
    private var prevSizeY = 0
    private var prevPosX = 0
    private var prevPosY = 0

    /**
     * Stage size and position setters
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

            /*
             * Adjust the wrapper and applet size and positions within the frame.
             */
            gui.fxFrameWrapper.setSize(stage.width, gui.fxFrameWrapper.height)
            gui.currentApplet.setLocation(
                stage.width - ((stage.width / 2) + (gui.currentApplet.width / 2)) - 1,
                gui.currentApplet.y
            )
        }

    private var stageHeight: Int
        get() = stage.height
        set(value) {
            stage.setSize(stage.width, value)

            /*
             * Adjust the wrapper and applet size and positions within the frame.
             */
            gui.fxFrameWrapper.setSize(gui.fxFrameWrapper.width, stage.height)
            gui.currentApplet.setLocation(
                gui.currentApplet.x,
                stage.height - ((stage.height / 2) + (gui.currentApplet.height / 2)) + 31
            )
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
            if(!movable.get()) return@setOnMousePressed
            if(event.isPrimaryButtonDown) {
                dx = event.sceneX.toInt()
                dy = event.sceneY.toInt()

                if(maximized.get()) {
                    dx = (prevSizeX * (event.sceneX / stage.width)).toInt()
                    dy = (prevSizeY * (event.sceneY / stage.height)).toInt()
                } else {
                    prevSizeX = stage.width
                    prevSizeY = stage.height
                    prevPosX = stage.x
                    prevPosY = stage.y
                }

                startX = event.screenX.toInt()
                startY = handle.prefHeight(stage.height.toDouble()).toInt()
            }
        }

        handle.setOnMouseDragged { event ->
            if(!movable.get()) return@setOnMouseDragged
            if(event.isPrimaryButtonDown) {
                if(resizing) return@setOnMouseDragged
                moving = true

                stageX = (event.screenX - dx).toInt()
                stageY = (event.screenY - dy).toInt()

                if(maximized.get()) {
                    stageWidth = prevSizeX
                    stageHeight = prevSizeY
                    setMaximizedState(false)
                }
            }
        }

        handle.setOnMouseReleased { event ->
            if(!movable.get()) return@setOnMouseReleased
            if(event.button == MouseButton.PRIMARY) {
                moving = false

                if(!maximized.get()) {
                    prevSizeX = stage.width
                    prevSizeY = stage.height
                    prevPosX = stage.x
                    prevPosY = stage.y
                }
            }
        }
    }

    /**
     * Toggles whether the window is maximized or not.
     */
    fun toggleMaximize() {
        /*
         * Reset the movable property.
         */
        movable.set(true)

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        if(maximized.get()) {
            stageWidth = prevSizeX
            stageHeight = prevSizeY
            stageX = prevPosX
            stageY = prevPosY
            setMaximizedState(false)
        } else {
            prevSizeX = stage.width
            prevSizeY = stage.height
            prevPosX = stage.x
            prevPosY = stage.y
            /*
             * Set the stage location and size for when we maximize the
             * window.
             */
            stageX = 0
            stageY = 0
            stageWidth = screenSize.width
            stageHeight = screenSize.height
            setMaximizedState(true)
        }
    }

    fun minimize() {
        movable.set(true)
        stage.state = JFrame.ICONIFIED
    }

    private fun setMaximizedState(state: Boolean) {
        this.maximized.set(state)
    }

}