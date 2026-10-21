package com.studiodraug.refproj.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import java.io.File

internal const val BASE_PACKAGE = "com.studiodraug.refproj"

/** The five test types of CLAUDE.md §2. Every test class is preceded by one of these comments. */
internal val TEST_TYPE_LABELS =
    listOf("// UNIT TEST", "// INTEGRATION TEST", "// UI TEST", "// SCREENSHOT TEST", "// ARCHITECTURE TEST")

/** KMP and JVM test source sets: `commonTest`, `androidHostTest`, `iosTest`, `androidDeviceTest`, `test`, … */
private val TEST_SOURCE_SET = Regex("""/src/(test|[a-zA-Z]+Test)/""")

internal val KoFileDeclaration.isTestSource: Boolean
    get() = TEST_SOURCE_SET.containsMatchIn(path.replace('\\', '/'))

/** Every Kotlin file of the repository, without build outputs and build-logic (Gradle code, not app code). */
internal val allSources: KoScope by lazy {
    Konsist.scopeFromProject().slice { !it.path.replace('\\', '/').contains("/build-logic/") }
}

internal val productionSources: KoScope by lazy { allSources.slice { !it.isTestSource } }

internal val testSources: KoScope by lazy { allSources.slice { it.isTestSource } }

/** The repository root: Konsist resolves the project root the same way (the directory with settings.gradle.kts). */
internal val repositoryRoot: File by lazy {
    generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").isFile }
}

/** Feature module names, from the directories under `feature/`. */
internal val featureModules: List<String> by lazy {
    File(repositoryRoot, "feature")
        .listFiles { file -> File(file, "build.gradle.kts").isFile }
        .orEmpty()
        .map { it.name }
        .sorted()
}

/** Main (non-test) Kotlin files of one module directory, e.g. `feature/login`. */
internal fun productionSourcesOf(moduleDir: String): KoScope =
    productionSources.slice { it.path.replace('\\', '/').contains("/$moduleDir/src/") }

private val BLOCK_COMMENT = Regex("""/\*[\s\S]*?\*/""")
private val LINE_COMMENT = Regex("""//[^\n]*""")

/** Source text without comments, so KDoc examples and commented-out code never trip a rule. */
internal fun String.withoutComments(): String = replace(BLOCK_COMMENT, "").replace(LINE_COMMENT, "")
