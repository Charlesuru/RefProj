import com.studiodraug.refproj.buildlogic.lib
import com.studiodraug.refproj.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A feature module = one user-facing capability (screens + ViewModels).
 *
 * Rules (enforced by architecture tests):
 *  - A feature may depend on core and domain modules.
 *  - A feature must NEVER depend on another feature or on data.
 *
 * Every feature gets the design system, the MVVM base (core:ui), route keys (core:navigation),
 * Koin for ViewModel injection, and the shared test doubles.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("refproj.kmp.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:designsystem"))
                    implementation(project(":core:navigation"))
                    implementation(project(":core:ui"))
                    implementation(project.dependencies.platform(libs.lib("koin-bom")))
                    implementation(libs.lib("koin-core"))
                    implementation(libs.lib("koin-compose-viewmodel"))
                }
                sourceSets.getByName("commonTest").dependencies {
                    implementation(project(":core:testing"))
                }
            }
        }
}
