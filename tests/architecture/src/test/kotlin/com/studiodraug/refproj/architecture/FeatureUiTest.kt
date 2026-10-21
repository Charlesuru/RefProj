package com.studiodraug.refproj.architecture

import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

// ARCHITECTURE TEST

/**
 * Feature UI uses the design system and string resources (CLAUDE.md §2): no raw colours, no dp/sp
 * literals and no hard-coded UI text in feature modules. Sample content for the screen catalog and IDE
 * previews (`*Previews.kt`, `@Preview`) may use literal text. Formatted data such as `"${time}  ${tag}"` is not text.
 */
class FeatureUiTest {
    private val featureFiles = featureModules.flatMap { productionSourcesOf("feature/$it").files }

    @Test
    fun features_useNoRawColours() {
        featureFiles.assertFalse(additionalMessage = "Use RefTheme colours from core:designsystem") { file ->
            RAW_COLOUR.containsMatchIn(file.text.withoutComments())
        }
    }

    @Test
    fun features_useNoDimensionLiterals() {
        featureFiles.assertFalse(additionalMessage = "Use spacing/size tokens from core:designsystem") { file ->
            DIMENSION_LITERAL.containsMatchIn(file.text.withoutComments())
        }
    }

    @Test
    fun features_hardCodeNoUiText() {
        val screens = featureFiles.filterNot { it.name.endsWith("Previews") }
        screens.assertFalse(additionalMessage = "Put UI text in composeResources strings.xml (English + Swedish)") { file ->
            file
                .functions(includeNested = false, includeLocal = false)
                .filterNot { function -> function.annotations.any { it.name == "Preview" } }
                .any { function -> HARD_CODED_TEXT.findAll(function.text.withoutComments()).any { it.isWords() } }
        }
    }

    /** True when the literal has letters outside its string templates, i.e. it is text, not formatted data. */
    private fun MatchResult.isWords(): Boolean = groupValues[LITERAL_GROUP].replace(TEMPLATE, "").any { it.isLetter() }

    private companion object {
        val RAW_COLOUR = Regex("""\bColor\(\s*0x|\bColor\.(Red|Green|Blue|Black|White|Gray|Yellow|Cyan|Magenta)\b""")
        val DIMENSION_LITERAL = Regex("""\b\d+(\.\d+)?\.(dp|sp)\b""")

        /** `Text("…")`, `Text(text = "…")`, `label = "…"`, `title = "…"`, `contentDescription = "…"`; group 1 = the literal. */
        val HARD_CODED_TEXT =
            Regex("""(?:\bText\(\s*(?:text\s*=\s*)?|\b(?:label|title|placeholder|contentDescription)\s*=\s*)"((?:[^"\\]|\\.)*)"""")
        const val LITERAL_GROUP = 1
        val TEMPLATE = Regex("""\$\{[^}]*\}|\$\w+""")
    }
}
