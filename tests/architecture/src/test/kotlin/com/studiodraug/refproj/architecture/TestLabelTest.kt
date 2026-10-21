package com.studiodraug.refproj.architecture

import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test
import kotlin.test.assertTrue as assertThat

// ARCHITECTURE TEST

/**
 * CLAUDE.md §2: every test class is marked with its type, so a reader (and CI reports) can tell a unit
 * test from an integration, UI, screenshot or architecture test. The label is a line comment directly
 * above the class; only KDoc and annotations may stand between them.
 */
class TestLabelTest {
    private val testClasses =
        testSources.classes(includeNested = false).filter { declaration ->
            declaration.functions().any { function -> function.annotations.any { it.name == "Test" } }
        }

    @Test
    fun testClasses_areFound() {
        assertThat(testClasses.size >= MIN_EXPECTED_TEST_CLASSES, "Found ${testClasses.size} test classes")
    }

    @Test
    fun everyTestClass_isLabelledWithItsType() {
        testClasses.assertTrue(
            additionalMessage = "Put one of ${TEST_TYPE_LABELS.joinToString()} on the line above the class (CLAUDE.md §2)",
        ) { it.testTypeLabel() != null }
    }

    /** The label comment above the class declaration, skipping blank lines, KDoc and annotations. */
    private fun KoClassDeclaration.testTypeLabel(): String? {
        val lines = containingFile.text.lines()
        val declarationLine = lines.indexOfFirst { DECLARATION.matches(it) && Regex("""\bclass\s+$name\b""").containsMatchIn(it) }
        return lines
            .subList(0, declarationLine.coerceAtLeast(0))
            .asReversed()
            .map { it.trim() }
            .firstOrNull { line -> line.isNotEmpty() && !line.startsWith("@") && !line.startsWith("*") && !line.startsWith("/**") }
            ?.takeIf { it in TEST_TYPE_LABELS }
    }

    private companion object {
        const val MIN_EXPECTED_TEST_CLASSES = 30
        val DECLARATION = Regex("""^\s*(\w+\s+)*class\s+.*""")
    }
}
