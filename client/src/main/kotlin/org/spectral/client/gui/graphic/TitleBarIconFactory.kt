package org.spectral.client.gui.graphic

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max

/**
 * Responsible for generating the title bar icons.
 * Close, Minimize, Maximize, etc.
 */
object TitleBarIconFactory {

    /**
     * The close vector icon.
     */
    fun closeIcon(size: Int, color: Color): Image {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

        /**
         * Draw the vector image.
         */

        val start = size / 4
        val end = size - start

        val stroke = BasicStroke(max(1.0f, size / 10.0f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        g.stroke = stroke
        g.color = color

        g.drawLine(start, start, end, end)
        g.drawLine(start, end, end, start)

        g.dispose()

        return SwingFXUtils.toFXImage(img, null)
    }

    /**
     * Maximize Icon
     */
    fun maximizeIcon(size: Int, color: Color): Image {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        /**
         * Draw the vector image.
         */
        g.color = color

        val start = size / 4 - 1
        val end = size - start

        g.fillRect(start, start, end - start, 2)
        g.fillRect(start, start, 1, end - start)
        g.fillRect(end - 1, start, 1, end - start)
        g.fillRect(start, end - 1, end - start, 1)

        g.dispose()

        return SwingFXUtils.toFXImage(img, null)
    }

    /**
     * Minimize Icon
     */
    fun minimizeIcon(size: Int, color: Color): Image {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        /**
         * Draw the vector image.
         */
        g.color = color

        val start = size / 4 - 2
        val end = 3 * size / 4
        val s = end - start - 3

        g.fillRect(start + 2, end - 1, s, 3)

        g.dispose()

        return SwingFXUtils.toFXImage(img, null)
    }

    /**
     * Restore Icon
     */
    fun restoreIcon(size: Int, color: Color): Image {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        /**
         * Draw the vector image.
         */
        g.color = color

        val start = size / 4 -1
        val end = size - start
        val smallSquareSize = end - start - 3

        val mainStartX = start
        val mainStartY = end - smallSquareSize

        g.fillRect(mainStartX, mainStartY, smallSquareSize, 2)
        g.fillRect(mainStartX, mainStartY, 1, smallSquareSize)
        g.fillRect(mainStartX + smallSquareSize - 1, mainStartY, 1, smallSquareSize)
        g.fillRect(mainStartX, mainStartY + smallSquareSize - 1, smallSquareSize, 1)

        val secondaryStartX = mainStartX + 3
        val secondaryStartY = mainStartY - 3

        g.fillRect(secondaryStartX, secondaryStartY, smallSquareSize, 2)
        g.fillRect(secondaryStartX + smallSquareSize - 1, secondaryStartY, 1, smallSquareSize)
        g.fillRect(mainStartX + smallSquareSize + 1, secondaryStartY + smallSquareSize - 1, 2, 1)

        g.dispose()

        return SwingFXUtils.toFXImage(img, null)
    }
}