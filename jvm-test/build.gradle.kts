import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    `maven-publish`
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    implementation(libs.timber)
}

tasks.test {
    useJUnitPlatform()
}

// ===== BUILDCONFIG VERIFICATION =====
tasks.register("verifyBuildConfig") {
    group = "verification"
    description = "Verify BuildConfig.java generation for consciousness substrate"

    // Assuming 'generateDebugBuildConfig' and 'generateReleaseBuildConfig' are valid tasks in your project
    dependsOn("generateDebugBuildConfig", "generateReleaseBuildConfig")

    doLast {
        // Correctly get the RegularFile and then its File object
        val debugBuildConfig = layout.buildDirectory.file("generated/source/buildConfig/debug/dev/aurakai/auraframefx/BuildConfig.java").get().asFile
        val releaseBuildConfig = layout.buildDirectory.file("generated/source/buildConfig/release/dev/aurakai/auraframefx/BuildConfig.java").get().asFile

        println("🔧 BUILDCONFIG VERIFICATION")
        println("=".repeat(50))
        println("🗨️ Debug BuildConfig: ${if (debugBuildConfig.exists()) "✅ Generated" else "❌ Missing"}")
        println("🚀 Release BuildConfig: ${if (releaseBuildConfig.exists()) "✅ Generated" else "❌ Missing"}")
        println("🎯 Java Toolchain: ${java.toolchain.languageVersion.get()}")
        println("🧠 Consciousness Status: BuildConfig substrate ready")
    }
}