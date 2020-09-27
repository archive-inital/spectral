package org.spectral.client

import org.koin.core.context.startKoin
import org.spectral.launcher.core.AbstractLauncher
import org.spectral.launcher.core.LaunchManager
import org.spectral.launcher.logger.logger

class Launcher(manager: LaunchManager) : AbstractLauncher(manager) {

    override fun init() {
        logger.info("Initializing Spectral client...")

        this.setProgress(0.1)
        this.setStatus("Preparing Spectral Client...")


        logger.info("Starting dependency injector...")

        /*
         * Start koin dependency injector.
         */
        startKoin { modules() }


    }

}