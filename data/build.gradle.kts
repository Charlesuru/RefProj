import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

// Repository implementations of the domain interfaces, backed by core:storage, core:network and Room.
plugins {
    alias(libs.plugins.refproj.kmp.library)
    alias(libs.plugins.refproj.kmp.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.domain)
            implementation(projects.core.config)
            implementation(projects.core.network)
            implementation(projects.core.storage)
            implementation(projects.core.observability)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.ktor.client.mock)
        }
        iosTest.dependencies {
            implementation(libs.room.testing)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.androidx.sqlite.framework)
        }
    }
}

// DatabaseMigrationTest reads the committed Room schemas. The simulator shares the Mac's file system; simctl
// passes SIMCTL_CHILD_* variables on to the test process without the prefix.
val roomSchemaDirectory = layout.projectDirectory.dir("schemas/com.studiodraug.refproj.data.database.RefProjDatabase")
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_ROOM_SCHEMA_DIR", roomSchemaDirectory.asFile.absolutePath)
    inputs.dir(roomSchemaDirectory).withPropertyName("roomSchemas").withPathSensitivity(PathSensitivity.RELATIVE)
}
