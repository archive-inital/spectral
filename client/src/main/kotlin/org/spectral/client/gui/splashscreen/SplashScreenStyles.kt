package org.spectral.client.gui.splashscreen

import tornadofx.*

class SplashScreenStyles : Stylesheet() {

    companion object {
        val blackDark by cssclass()
        val whiteDarkText by cssclass()
    }

    init {
        blackDark {
            backgroundColor += c("#141A1F")
        }

        whiteDarkText {
            textFill = c("#6B859E")
        }
    }

}