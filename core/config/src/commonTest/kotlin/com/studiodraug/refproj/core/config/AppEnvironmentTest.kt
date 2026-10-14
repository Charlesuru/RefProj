package com.studiodraug.refproj.core.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UNIT TEST
class AppEnvironmentTest {
    @Test
    fun parse_isCaseInsensitiveAndTrims() {
        assertEquals(AppEnvironment.STAGE, AppEnvironment.parse(" stage "))
        assertEquals(AppEnvironment.DEV, AppEnvironment.parse("Dev"))
    }

    @Test
    fun parse_unknownValue_fails() {
        assertFailsWith<IllegalStateException> { AppEnvironment.parse("qa") }
    }

    @Test
    fun onlyProd_isProduction() {
        assertTrue(AppEnvironment.PROD.isProduction)
        AppEnvironment.entries.filter { it != AppEnvironment.PROD }.forEach { assertFalse(it.isProduction) }
    }
}
