import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.studiodraug.refproj.buildlogic.derivedNamespace
import com.studiodraug.refproj.buildlogic.lib
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * KMP module with Compose Multiplatform UI and Compose resources (strings, images, fonts).
 * Each module gets its own `Res` class in `<namespace>.resources` to avoid clashes.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("refproj.kmp.library")
            pluginManager.apply(libs.pluginId("compose-multiplatform"))
            pluginManager.apply(libs.pluginId("compose-compiler"))

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    androidResources.enable = true
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.lib("compose-runtime"))
                    implementation(libs.lib("compose-foundation"))
                    implementation(libs.lib("compose-ui"))
                    implementation(libs.lib("compose-material3"))
                    implementation(libs.lib("compose-components-resources"))
                    implementation(libs.lib("compose-ui-tooling-preview"))
                }
                // UI tests (runComposeUiTest) in commonTest run on the iOS simulator and, under Robolectric
                // (PlatformTest), on the Android host. The host needs an activity to host the composition.
                sourceSets.getByName("commonTest").dependencies {
                    implementation(libs.lib("compose-ui-test"))
                    implementation(project(":core:testing"))
                }
                sourceSets.getByName("androidHostTest").dependencies {
                    implementation(libs.lib("androidx-compose-ui-test-manifest"))
                }
            }

            dependencies {
                // Android Studio renders @Preview with the module's own Android runtime classpath, so the
                // renderer (ui-tooling) must be there. It is not exported to consumers or the release app.
                add("androidRuntimeClasspath", libs.lib("compose-ui-tooling"))
            }

            extensions.configure<ComposeExtension> {
                (this as ExtensionAware).extensions.configure<ResourcesExtension> {
                    packageOfResClass = "$derivedNamespace.resources"
                }
            }
        }
}
