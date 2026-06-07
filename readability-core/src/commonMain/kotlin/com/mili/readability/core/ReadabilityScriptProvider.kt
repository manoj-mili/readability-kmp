package com.mili.readability.core

/**
 * Provides the JavaScript payload a host browser injects into its page context.
 */
interface ReadabilityScriptProvider {
    fun getReadabilityScript(): String
}
