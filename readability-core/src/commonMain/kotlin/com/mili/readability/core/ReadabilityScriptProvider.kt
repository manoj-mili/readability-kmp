package com.mili.readability.core

/**
 * Provides the JavaScript payload a host browser injects into its page context.
 */
interface ReadabilityScriptProvider {
    fun getReadabilityScript(): String
}

/**
 * Creates the default [ReadabilityScriptProvider] backed by the bundled
 * Mozilla Readability script.
 */
fun defaultReadabilityScriptProvider(): ReadabilityScriptProvider =
    MozillaReadabilityScriptProvider()
