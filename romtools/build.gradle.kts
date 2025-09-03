// ==== GENESIS PROTOCOL - ROM TOOLS ====
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
    compileSdk = 36 // Required for AGP 9 and dependency resolution
}

// ROM Tools output directory configuration
val romToolsOutputDirectory: DirectoryProperty = 
    project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

dependencies {
    api(project(":core-module"))
    implementation(project(":secure-comm"))
    dependencies {
        // Xposed & LSPosed Framework APIs (Provided at runtime, not bundled)
        // Note: For YukiHookAPI, ensure jitpack.io is in your settings.gradle.kts
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        // Compose - Bill of Materials (BOM)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.bundles.compose)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.navigation.compose)

        // Core AndroidX
        implementation(libs.bundles.androidx.core)

        // Dependency Injection - Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)

        // Coroutines & Networking
        implementation(libs.bundles.coroutines)
        implementation(libs.bundles.network)

        // Database - Room
        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        // Firebase
        implementation(libs.bundles.firebase)

        // Utilities
        implementation(libs.timber)
        implementation(libs.coil.compose)

        // Debugging Tools
        debugImplementation(libs.leakcanary.android)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)

        // Unit Testing
        testImplementation(libs.bundles.testing)
        testImplementation(libs.hilt.android.testing)

        // Instrumented Testing
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        androidTestImplementation(libs.hilt.android.testing)
        kspAndroidTest(libs.hilt.compiler)
    }

tasks.register("romStatus") { group = "aegenesis"; doLast { println("🛠️ ROM TOOLS - Ready!") } }
}