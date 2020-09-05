package org.spectral.deobfuscator

import org.tinylog.kotlin.Logger
import java.applet.Applet
import java.applet.AppletContext
import java.applet.AppletStub
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import javax.swing.JFrame

/**
 * Represents a test jagex client using [gamepack] gamepack jar file.
 *
 * @property gamepack File
 * @constructor
 */
class TestClient(val gamepack: File) {

    fun start() {
        Logger.info("Starting test client with gamepack JAR: 'gamepack.jar'.")

        val params = this.crawlJavConfig()
        val classloader = URLClassLoader(arrayOf(gamepack.toURI().toURL()))
        val main = params["initial_class"]!!.replace(".class", "")
        val applet = classloader.loadClass(main).newInstance() as Applet

        applet.background = Color.BLACK
        applet.preferredSize = Dimension(params["applet_minwidth"]!!.toInt(), params["applet_minheight"]!!.toInt())
        applet.size = applet.preferredSize
        applet.layout = null
        applet.setStub(object : AppletStub {
            override fun getCodeBase(): URL = URL(params["codebase"])
            override fun getDocumentBase(): URL = URL(params["codebase"])
            override fun isActive(): Boolean = true
            override fun appletResize(width: Int, height: Int) {
                applet.size = Dimension(width, height)
            }
            override fun getAppletContext(): AppletContext? = null
            override fun getParameter(name: String): String? = params[name]
        })
        applet.isVisible = true
        applet.init()

        val frame = JFrame()
        frame.title = "Test Client"
        frame.layout = GridLayout(1, 0)
        frame.add(applet)
        frame.pack()
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    /**
     * Gets the JAV_CONFIG from jagex HTTP server.
     *
     * @return Map<String, String>
     */
    private fun crawlJavConfig(): Map<String, String> {
        val params = hashMapOf<String, String>()
        val lines = URL(CODEBASE + "jav_config.ws").readText().split("\n")

        lines.forEach {
            var line = it
            if(line.startsWith("param=")) {
                line = line.substring(6)
            }
            val idx = line.indexOf("=")
            if(idx >= 0) {
                params[line.substring(0, idx)] = line.substring(idx + 1)
            }
        }

        return params
    }

    companion object {
        /**
         * The Jagex codebase URL.
         */
        private const val CODEBASE = "http://oldschool7.runescape.com/"

        @JvmStatic
        fun main(args: Array<String>) {
            if(args.isEmpty()) return
            val client = TestClient(File(args[0]))
            client.start()
        }
    }
}