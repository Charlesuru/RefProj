package com.studiodraug.refproj.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

// UNIT TEST

/**
 * Every module's strings exist in English (default) and Swedish, with the same keys and the same
 * placeholders. Runs on the JVM only, since it reads the resource files from the repository.
 */
class TranslationsTest {
    private val repoRoot: File =
        generateSequence(File("").absoluteFile) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").isFile }
            ?: fail("Repository root not found")

    private val defaultFiles: List<File> =
        repoRoot
            .walkTopDown()
            .onEnter { it.name !in setOf("build", ".gradle", ".git", "vendor", "node_modules") }
            .filter { it.path.endsWith("composeResources/values/strings.xml") }
            .toList()

    @Test
    fun stringFiles_areFound() {
        assertTrue(defaultFiles.isNotEmpty(), "No composeResources/values/strings.xml found under $repoRoot")
    }

    @Test
    fun everyString_isTranslatedToSwedish_withTheSamePlaceholders() {
        val problems =
            defaultFiles.flatMap { english ->
                val module = english.relativeTo(repoRoot).path.substringBefore("/src/")
                val swedishFile = File(english.parentFile.parentFile, "values-sv/strings.xml")
                if (!swedishFile.isFile) return@flatMap listOf("$module: values-sv/strings.xml is missing")
                val en = readStrings(english)
                val sv = readStrings(swedishFile)
                (en.keys - sv.keys).map { "$module: '$it' is not translated to Swedish" } +
                    (sv.keys - en.keys).map { "$module: '$it' exists only in Swedish" } +
                    en.keys
                        .intersect(sv.keys)
                        .filter { placeholders(en.getValue(it)) != placeholders(sv.getValue(it)) }
                        .map { "$module: '$it' has different placeholders in Swedish" }
            }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    private fun readStrings(file: File): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    private fun placeholders(value: String): List<String> =
        PLACEHOLDER
            .findAll(value)
            .map { it.value }
            .sorted()
            .toList()

    private companion object {
        val PLACEHOLDER = Regex("%(\\d+\\$)?[sd]")
    }
}
