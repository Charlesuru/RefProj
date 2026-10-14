import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.version
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Repository-wide static analysis, applied once on the root project.
 *
 *  - `./gradlew spotlessCheck` / `spotlessApply` — formatting (ktlint + Compose rules)
 *  - `./gradlew detekt` — static analysis (complexity, bugs, style)
 *
 * Running these at the root (instead of per module) gives one fast task and one report for CI.
 */
class QualityConventionPlugin : Plugin<Project> {
    private companion object {
        /**
         * Never scanned: build outputs (Xcode leaves symlinks there that dangle after a Gradle clean),
         * caches, vendored Ruby gems and generated Xcode projects. Matching the directory itself prunes
         * the walk, so the walker never enters it.
         */
        val EXCLUDED =
            listOf(
                "**/build",
                "**/build/**",
                "**/.gradle/**",
                "**/.kotlin/**",
                "vendor/**",
                "**/node_modules/**",
                "iosApp/Pods/**",
                "iosApp/*.xcodeproj/**",
            )
    }

    override fun apply(target: Project) =
        with(target) {
            check(this == rootProject) { "refproj.quality must only be applied to the root project" }

            pluginManager.apply("io.gitlab.arturbosch.detekt")
            pluginManager.apply("com.diffplug.spotless")

            // Spotless' targetExclude() filters only after walking the whole tree, so every
            // target is a pre-filtered file tree instead.
            fun sources(vararg patterns: String) =
                fileTree(rootDir) {
                    include(patterns.toList())
                    exclude(EXCLUDED)
                }
            val kotlinSources = sources("**/src/**/*.kt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                parallel = true
                config.setFrom(file("config/detekt/detekt.yml"))
                baseline = file("config/detekt/baseline.xml")
                source.setFrom(kotlinSources)
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "17"
                reports {
                    html.required.set(true)
                    sarif.required.set(true) // uploaded to GitHub code scanning in CI
                    xml.required.set(false)
                    txt.required.set(false)
                    md.required.set(false)
                }
            }

            // detekt 1.23.x embeds the Kotlin 2.0 compiler. Pin it so it does not
            // collide with the project's newer Kotlin version on the detekt classpath.
            val detektKotlin = libs.version("detekt-kotlin")
            configurations.matching { it.name == "detekt" }.configureEach {
                resolutionStrategy.eachDependency {
                    if (requested.group == "org.jetbrains.kotlin") useVersion(detektKotlin)
                }
            }

            val ktlintVersion = libs.version("ktlint")
            val composeRules = "io.nlopez.compose.rules:ktlint:${libs.version("compose-rules")}"
            extensions.configure<SpotlessExtension> {
                // The repo is LF-only (.gitattributes, .editorconfig). The default GIT_ATTRIBUTES mode
                // walks the whole working tree, including symlinks Xcode leaves in build directories.
                lineEndings = LineEnding.UNIX
                kotlin {
                    target(sources("**/*.kt"))
                    ktlint(ktlintVersion).customRuleSets(listOf(composeRules))
                }
                kotlinGradle {
                    target(sources("**/*.gradle.kts"))
                    ktlint(ktlintVersion)
                }
                format("misc") {
                    target(sources("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore", "**/*.toml"))
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
}
