package org.spectral.common

import org.koin.core.KoinComponent

/**
 * Simply an interface alias for [KoinComponent].
 *
 * The koin component interface gives a class access to the dependency
 * injection API, however to be more verbose on what it's purpose is and its
 * reason for being implemented.
 */
interface Injectable : KoinComponent