import com.studiodraug.refproj.buildlogic.TOTAL_COVERAGE_KEY
import com.studiodraug.refproj.buildlogic.coverageFloor
import com.studiodraug.refproj.buildlogic.excludeNonLogic
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.pluginId
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Merged code coverage of every module, applied once on the root project.
 *
 *  - `./gradlew koverHtmlReport` / `koverXmlReport` — one report for the whole app in `build/reports/kover`
 *  - `./gradlew koverVerify` — every module's floor plus the floor for the total
 *    (`config/kover/coverage-floors.properties`)
 *
 * Modules join automatically: `refproj.kmp.library` applies Kover, and this plugin aggregates every
 * project that has it.
 */
class CoverageConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            check(this == rootProject) { "refproj.coverage must only be applied to the root project" }
            val koverPluginId = libs.pluginId("kover")
            pluginManager.apply(koverPluginId)

            subprojects {
                val module = this
                pluginManager.withPlugin(koverPluginId) { rootProject.dependencies.add("kover", module) }
            }

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters { excludes { excludeNonLogic() } }
                    verify { rule("Line coverage of all modules") { minBound(coverageFloor(TOTAL_COVERAGE_KEY)) } }
                }
            }
        }
}
