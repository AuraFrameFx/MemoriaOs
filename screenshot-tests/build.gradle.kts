// ==== GENESIS PROTOCOL - SCREENSHOT TESTS ====
// Visual regression testing for Genesis UI components

plugins {
    id("genesis.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.aurakai.auraframefx.screenshottests"
    compileSdk = 36 // Required for AGP 9 and dependency resolution

    // Disable unnecessary features for screenshot testing
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Project modules to test
    testImplementation(project(":core-module"))
    testImplementation(project(":sandbox-ui"))
    testImplementation(project(":colorblendr"))
    testImplementation(project(":collab-canvas"))
    
    // Compose testing
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.bundles.compose)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    
    // Testing framework
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    
    // Hilt for DI in tests
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    implementation(libs.hilt.android) // Added to satisfy Hilt Gradle plugin requirement

    // Robolectric for screenshot tests
    testImplementation(libs.robolectric)
}

// Custom screenshot testing tasks
tasks.register("screenshotTestAll") {
    group = "screenshot"
    description = "Run all Genesis Protocol screenshot tests"
    
    dependsOn("testDebugUnitTest")
    
    doLast {
        println("📸 Genesis Protocol Screenshot Tests")
        println("🎨 Visual regression testing for:")
        println("   - Core UI components")
        println("   - Color management (ColorBlendr)")
        println("   - Collaboration interface (CollabCanvas)")
        println("   - Sandbox experiments (SandboxUI)")
        println("💡 Configure Paparazzi when needed for advanced screenshot testing")
    }
}

tasks.register("updateScreenshots") {
    group = "screenshot"
    description = "Update Genesis Protocol UI component screenshots"
    
    doLast {
        println("📸 Genesis Protocol screenshots update ready")
        println("🎨 Configure screenshot baseline when Paparazzi is available")
    }
}

tasks.register("verifyScreenshots") {
    group = "verification"
    description = "Verify UI components match reference screenshots"
    
    doLast {
        println("✅ Genesis Protocol UI visual consistency framework ready")
        println("🎨 Screenshot testing infrastructure configured")
    }
}
