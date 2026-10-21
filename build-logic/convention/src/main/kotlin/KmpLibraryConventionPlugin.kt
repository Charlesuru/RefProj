import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.studiodraug.refproj.buildlogic.configureCoverage
import com.studiodraug.refproj.buildlogic.derivedNamespace
import com.studiodraug.refproj.buildlogic.lib
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.pinToolClasspathSecurityVersions
import com.studiodraug.refproj.buildlogic.pluginId
import com.studiodraug.refproj.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base for every Kotlin Multiplatform module (core, domain, data, feature).
 *
 * Targets: Android + iOS (device and Apple-silicon simulator).
 * Intel simulators (iosX64) are intentionally not built — Xcode 26 only ships on Apple silicon.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-multiplatform"))
            pluginManager.apply(libs.pluginId("android-kmp-library"))
            pinToolClasspathSecurityVersions()
            configureCoverage()

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    namespace = derivedNamespace
                    compileSdk = libs.version("android-compileSdk").toInt()
                    minSdk = libs.version("android-minSdk").toInt()
                    withHostTest {}
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
                }

                iosArm64()
                iosSimulatorArm64()

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                sourceSets.getByName("commonTest").dependencies {
                    implementation(libs.lib("kotlin-test"))
                    implementation(libs.lib("kotlinx-coroutines-test"))
                }
            }
        }
}
