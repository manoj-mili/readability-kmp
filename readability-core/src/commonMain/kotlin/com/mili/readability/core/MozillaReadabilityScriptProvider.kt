package com.mili.readability.core

internal class MozillaReadabilityScriptProvider : ReadabilityScriptProvider {
    override fun getReadabilityScript(): String {
        return ReadabilityBundled.READABILITY_JS
    }
}
