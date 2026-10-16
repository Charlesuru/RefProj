import androidx.room.gradle.RoomExtension
import com.studiodraug.refproj.buildlogic.lib
import com.studiodraug.refproj.buildlogic.libs
import com.studiodraug.refproj.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Room (KMP) on top of `refproj.kmp.library`: runtime, bundled SQLite driver (same SQLite version
 * on every platform and OS release) and the Room compiler via KSP for each target.
 *
 * Schemas are exported to `<module>/schemas` and **committed**: they document every database
 * version and are the input for migration tests.
 */
class KmpRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("refproj.kmp.library")
            pluginManager.apply(libs.pluginId("ksp"))
            pluginManager.apply(libs.pluginId("room"))

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.lib("room-runtime"))
                    implementation(libs.lib("androidx-sqlite-bundled"))
                }
            }

            dependencies {
                // One KSP configuration per target (KMP has no shared "ksp" configuration for Room).
                listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { configuration ->
                    add(configuration, libs.lib("room-compiler"))
                }
            }
        }
}
