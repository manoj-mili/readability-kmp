package com.mili.readability.core

import kotlin.test.Test
import kotlin.test.assertTrue

class MozillaReadabilityScriptProviderTest {
    @Test
    fun providesBundledReadabilityScript() {
        val script = MozillaReadabilityScriptProvider().getReadabilityScript()

        assertTrue(script.contains("function Readability"))
    }
}
