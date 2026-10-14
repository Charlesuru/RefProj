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
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("refproj.kmp.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:designsystem"))
                    implementation(libs.lib("androidx-lifecycle-viewmodel-compose"))
                    implementation(libs.lib("androidx-lifecycle-runtime-compose"))
                }
            }
        }
}
