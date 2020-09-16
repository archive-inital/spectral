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
import java.applet.Applet
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.ImageIcon
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
    internal val frame = JFrame()

    /**
     * JavaFX
     */
    private lateinit var stage: Stage
    private lateinit var app: SpectralApp
    lateinit var fxFrameWrapper: JFXPanel private set

    lateinit var currentApplet: Applet

    fun showFrame() {
        logger.info("Building Java Swing processes.")
        SwingUtilities.invokeLater {
            frame.layout = null
            frame.title = "Spectral"

            fxFrameWrapper.setBounds(0, 0, currentApplet.width + 6, currentApplet.height + 72)
            currentApplet.setBounds(3, 68, currentApplet.width, currentApplet.height)

            frame.add(currentApplet)
            frame.add(fxFrameWrapper)

            frame.size = Dimension(fxFrameWrapper.width, fxFrameWrapper.height)
            frame.minimumSize = Dimension(fxFrameWrapper.width, fxFrameWrapper.height)

            frame.isUndecorated = true
            frame.iconImage = ImageIcon(ClassLoader.getSystemResource("spectral-app.png")).image

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