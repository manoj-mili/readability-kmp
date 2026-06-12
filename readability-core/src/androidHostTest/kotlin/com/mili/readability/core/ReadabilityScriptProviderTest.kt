package com.mili.readability.core

import kotlin.test.Test
import kotlin.test.assertTrue

class ReadabilityScriptProviderTest {
    @Test
    fun defaultProviderReturnsBundledScript() {
        val script = defaultReadabilityScriptProvider().getReadabilityScript()

        assertTrue(script.contains("function Readability"))
    }
}
