package org.spectral.client.gui.controller

import javafx.beans.property.SimpleBooleanProperty
import org.spectral.client.gui.view.FXFrameView
import tornadofx.Controller

/**
 * Responsible for providing movement logic to the Spectral JavaFX
 * viewport or Frame window.
 *
 * @author Kyle Escobar
 */
class FXFrameController : Controller() {

    val maximized = SimpleBooleanProperty(false)

    private val frame: FXFrameView by inject()
    private val stage get() = primaryStage
    private val handle = frame.titleBar

    /*
     * Internal states
     */
    private var moving = false
    private var resizing = false

    private var prevSizeX = 0.0
    private var prevSizeY = 0.0

    private var prevPosX = 0.0
    private var prevPosY = 0.0

    /**
     * Initializes the FX Frame
     */
    fun init() {
        prevSizeX = stage.width
        prevSizeY = stage.height
        prevPosX = stage.x
        prevPosY = stage.y

        this.initResizeControls()
        this.initMoveControls()
    }

    /**
     * Initializes the resize listeners and variables.
     */
    private fun initResizeControls() {

    }

    /**
     * Initializes the window movement listeners and variables.
     */
    private fun initMoveControls() {

    }
}