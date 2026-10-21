import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.studiodraug.refproj.buildlogic.lib
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * On-device Android tests (`src/androidDeviceTest`) for the few things only a real Android runtime can
 * check: the Android Keystore, the bundled SQLite native library, file-backed databases.
 *
 *  - `./gradlew <module>:ciDeviceAndroidDeviceTest` — on a Gradle Managed Device (emulator started and stopped
 *    by Gradle; what the nightly workflow runs)
 *  - `ANDROID_SERIAL=emulator-5554 ./gradlew <module>:connectedAndroidDeviceTest` — on a running emulator
 *
 * Not part of `allTests`: device tests need an emulator and run nightly, not on every pull request.
 */
class KmpDeviceTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("refproj.kmp.library")

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    withDeviceTest {
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        managedDevices.localDevices.create(MANAGED_DEVICE) {
                            device = "Pixel 6"
                            sdkVersion = libs.version("android-deviceTestSdk").toInt()
                            // Automated Test Device image: headless and trimmed for tests, starts faster.
                            systemImageSource = "aosp-atd"
                        }
                    }
                }

                sourceSets.getByName("androidDeviceTest").dependencies {
                    implementation(libs.lib("kotlin-test"))
                    implementation(libs.lib("kotlinx-coroutines-test"))
                    implementation(libs.lib("androidx-test-runner"))
                    implementation(libs.lib("androidx-test-core"))
                    implementation(libs.lib("androidx-test-ext-junit"))
                }
            }
        }

    private companion object {
        /** Task prefix: `ciDeviceAndroidDeviceTest`. */
        const val MANAGED_DEVICE = "ciDevice"
    }
}
