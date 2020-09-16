package org.spectral.client.gui

import org.koin.core.inject
import org.spectral.client.Spectral
import org.spectral.client.common.Defaults
import org.spectral.common.Injectable
import org.spectral.common.logger.logger
import org.spectral.util.Platform
import java.applet.Applet
import java.applet.AppletContext
import java.applet.AppletStub
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.net.URL
import java.net.URLClassLoader

class AppletManager : Injectable {

    private val spectral: Spectral by inject()
    private val javConfig = spectral.javConfig

    val applets = mutableListOf<Applet>()

    fun createClient() {
        logger.info("Creating Jagex client wrapper.")

        val gamepackFile = Platform.currentPlatform.dataDir.resolve(Defaults.SPECTRAL_DIR).resolve("bin/gamepack-raw.jar").toFile()
        //val gamepackFile = File("gamepack-deob.jar")
        val classloader = URLClassLoader(arrayOf(gamepackFile.toURI().toURL()))
        val applet = classloader.loadClass(javConfig["initial_class"]!!.replace(".class","")).newInstance() as Applet
        applet.background = Color.BLACK
        applet.preferredSize = Dimension(javConfig["applet_minwidth"]!!.toInt(), javConfig["applet_minheight"]!!.toInt())
        applet.size = applet.preferredSize
        applet.layout = null
        applet.setStub(applet.stub)
        applet.isVisible = true
        applet.init()

        applets.add(applet)
    }

    private val Applet.stub: AppletStub get() = object : AppletStub {
        override fun isActive(): Boolean = true
        override fun getCodeBase(): URL? = URL(javConfig["codebase"])
        override fun getDocumentBase(): URL? = URL(javConfig["codebase"])
        override fun getAppletContext(): AppletContext? = null
        override fun appletResize(width: Int, height: Int) { this@stub.size = Dimension(width, height) }
        override fun getParameter(name: String): String? = javConfig[name]
    }
}