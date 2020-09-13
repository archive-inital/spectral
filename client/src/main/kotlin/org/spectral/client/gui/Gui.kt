package org.spectral.client.gui

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.stage.Stage
import org.koin.core.get
import org.koin.core.inject
import org.spectral.client.gui.view.FXFrameView
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import tornadofx.find
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

/**
 * Responsible for managing the Spectal Gui.
 */
class Gui : Injectable {

    private val appletManager: AppletManager by inject()

    /**
     * The primary Java Swing frame the elements are embeded in.
     */
    val frame = JFrame()

    /**
     * JavaFX
     */
    private lateinit var stage: Stage
    private lateinit var app: SpectralApp
    private lateinit var fxFrameWrapper: JFXPanel

    fun showFrame() {
        logger.info("Building Java Swing processes.")
        SwingUtilities.invokeLater {
            frame.layout = BorderLayout()
            frame.title = "Spectral"

            frame.add(fxFrameWrapper, BorderLayout.PAGE_START)

            appletManager.createClient()

            val initialApplet = appletManager.applets.first()
            frame.add(initialApplet, BorderLayout.CENTER)
            frame.size = initialApplet.preferredSize
            frame.pack()

            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.setLocationRelativeTo(null)
            frame.isVisible = true

            frame.isAlwaysOnTop = true
            frame.isAlwaysOnTop = false
        }
    }

    private fun startJavaFX() {
        logger.info("Starting JavaFX processes.")

        SwingUtilities.invokeLater {
            fxFrameWrapper = JFXPanel()

            Platform.runLater {
                stage = Stage()
                app = SpectralApp()
                app.start(stage)

                fxFrameWrapper.scene = stage.scene
            }
        }
    }

    /**
     * Launches the Spectral Gui processes.
     */
    fun launch() {
        logger.info("Launching Spectral GUI.")
        this.startJavaFX()
    }
}