package com.mili.readability.core

class MozillaReadabilityScriptProvider : ReadabilityScriptProvider {
    override fun getReadabilityScript(): String {
        return ReadabilityBundled.READABILITY_JS
    }
}
