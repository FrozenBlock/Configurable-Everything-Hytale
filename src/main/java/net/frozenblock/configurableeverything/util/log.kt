package net.frozenblock.configurableeverything.util

import net.frozenblock.configurableeverything.CEPlugin

fun log(message: String, should: Boolean = true) {
    if (should)
        CEPlugin.LOGGER.atInfo().log(message)
}

fun logWarn(message: String, should: Boolean = true) {
    if (should)
        CEPlugin.LOGGER.atWarning().log(message)
}

fun logError(message: String, exception: Throwable? = null, should: Boolean = true) {
    if (should)
        CEPlugin.LOGGER.atSevere().log(message, exception)
}

fun logDebug(message: String, should: Boolean = true) {
    if (should)
        CEPlugin.LOGGER.atFine().log(message)
}