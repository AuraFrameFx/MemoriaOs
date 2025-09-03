
import dev.aurakai.gradle.tasks.VerifyRomToolsTask

 
plugins {
    id("genesis.android.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
}

// ROM Tools output directory configuration
val romToolsOutputDirectory: DirectoryProperty = 
    project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

dependencies {
    api(project(":core-module"))
    implementation(project(":secure-comm"))
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)
    implementation(libs.androidx.security.crypto)
    implementation(libs.timber)
    implementation(libs.gson)
    implementation(libs.utilities)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Define a shared directory property for ROM tools output
val romToolsOutputDirectory: DirectoryProperty =
    project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

// ROM Tools specific tasks
tasks.register<Copy>("copyRomTools") {
    from("src/main/resources")
    into(romToolsOutputDirectory) // Use the shared property with into()
    include("**/*.so", "**/*.bin", "**/*.img", "**/*.jar")
    includeEmptyDirs = false

    doFirst {
        val outputDir = romToolsOutputDirectory.get().asFile
        outputDir.mkdirs()
        logger.lifecycle("📁 ROM tools directory: ${outputDir.absolutePath}")
    }

    doLast {
        logger.lifecycle("✅ ROM tools copied to: ${romToolsOutputDirectory.get().asFile.absolutePath}")
    }
}


tasks.register<VerifyRomToolsTask>("verifyRomTools") {
    romToolsDir.set(romToolsOutputDirectory) // Set to the same shared property
    dependsOn("copyRomTools") // Explicitly depend on copyRomTools for clarity and reliability
    // Gradle should infer the dependency on copyRomTools because romToolsOutputDirectory
    // is an output of copyRomTools (via 'into') and an input here.
}

tasks.named("build") {
    dependsOn("verifyRomTools")
} 
