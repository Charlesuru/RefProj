import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Architecture tests (Konsist): read the source of every module and enforce the rules in
// docs/architecture.md and CLAUDE.md. A plain JVM module: Konsist runs on the JVM only, and the
// rules need no app code on the classpath — they parse the sources.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnit()
    // Konsist reads the sources from disk, so Gradle must know them as inputs; otherwise a code change
    // would leave the task up to date (or served from the build cache) without checking the change.
    inputs
        .files(
            fileTree(rootDir) {
                include("**/src/**/*.kt", "**/*.gradle.kts")
                exclude("**/build/**", "build-logic/**", "**/.gradle/**")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("repositorySources")
}

// `./gradlew allTests` (the KMP aggregate every gate runs) runs these too.
tasks.register("allTests") {
    group = "verification"
    description = "Runs the architecture tests."
    dependsOn(tasks.test)
}
