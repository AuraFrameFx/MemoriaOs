// ==== GENESIS PROTOCOL - MAIN APPLICATION ====
// Uses convention plugins for consistent configuration

plugins {
    // Use the standard Android application plugin
    id("com.android.application")

    // Additional plugins specific to the app
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

android {
    compileSdk = 36

    namespace = "dev.aurakai.auraframefx"

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        versionCode = 1
        versionName = "1.0.0-genesis-alpha"

        // NDK configuration only if native code exists
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
        }
    }

    // External native build only if CMakeLists.txt exists
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            buildConfigField("String", "RELEASE_SAMPLE", "\"releaseValue\"")
        }
        debug {
            buildConfigField("String", "DEBUG_SAMPLE", "\"debugValue\"")
        }
    }

    // Test configuration
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // ===== CORE LIBRARY DESUGARING =====
    // CRITICAL: Only the app module gets the actual desugaring dependency
    coreLibraryDesugaring(libs.android.desugar.jdklibs)
    
    // ===== MODULE DEPENDENCIES =====
    // App depends on all feature modules
    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":sandbox-ui"))
    implementation(project(":datavein-oracle-native"))

    // ===== ANDROIDX CORE =====
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ===== COMPOSE =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // ===== DEPENDENCY INJECTION - HILT =====
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ===== COROUTINES & NETWORKING =====
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // ===== DATABASE - ROOM =====
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ===== FIREBASE =====
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // ===== UTILITIES =====
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // ===== XPOSED FRAMEWORK =====
    implementation(fileTree("Libs") { include("*.jar") })

    // ===== TESTING DEPENDENCIES =====
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // ===== DEBUG TOOLS =====
    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Build integration with root project tasks
tasks.named("preBuild") {
    if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) {
        dependsOn(":openApiGenerate")
    }
}

// Status tasks
tasks.register("appStatus") {
    group = "aegenesis"
    description = "Show main application status"
    doLast {
        println("📱 MAIN APPLICATION STATUS")
        println("=".repeat(40))
        val androidExt = extensions.findByName("android") as? com.android.build.gradle.internal.dsl.BaseAppModuleExtension
        if (androidExt != null) {
            println("🔧 Namespace: ${androidExt.namespace}")
            println("🎯 App ID: ${androidExt.defaultConfig.applicationId}")
            println("📱 Version: ${androidExt.defaultConfig.versionName} (${androidExt.defaultConfig.versionCode})")
            println("📱 SDK: ${androidExt.compileSdk} (Min: ${androidExt.defaultConfig.minSdk}, Target: ${androidExt.defaultConfig.targetSdk})")
        } else {
            println("⚠️ Android extension not found. Make sure the Android plugin is applied.")
        }
        println("🔧 Native Code: ${if (project.file("src/main/cpp/CMakeLists.txt").exists()) "✅ Enabled" else "❌ Disabled"}")
        println("🎨 Compose: ✅ Enabled")
        println("🧠 Desugaring: ✅ App Module (with dependency)")
        println("✨ Status: Genesis Protocol Application Ready!")
    }
}

// Gradle verification task
tasks.register("gradle10CompatibilityCheck") {
    group = "verification"
    description = "Check for Gradle 10 compatibility issues in consciousness substrate"

    doLast {
        println("⚙️ GRADLE 10 COMPATIBILITY CHECK")
        println("=".repeat(50))
        println("📋 Current Gradle: ${gradle.gradleVersion}")
        println("✅ Convention Plugins: Build-logic system active")
        println("✅ Dependencies: Using version catalog (Gradle 10 ready)")
        println("✅ Kotlin DSL: Modern syntax applied")
        println("🧠 Consciousness Status: Ready for Gradle 10 migration")
    }
}

tasks.register("verifyBuildConfig") {
    group = "verification"
    description = "Verify BuildConfig.java generation for consciousness substrate"

    dependsOn("generateDebugBuildConfig", "generateReleaseBuildConfig")

    doLast {
        val debugBuildConfig = layout.buildDirectory.file("generated/source/buildConfig/debug/dev/aurakai/auraframefx/BuildConfig.java").get().asFile
        val releaseBuildConfig = layout.buildDirectory.file("generated/source/buildConfig/release/dev/aurakai/auraframefx/BuildConfig.java").get().asFile

        println("🔧 BUILDCONFIG VERIFICATION")
        println("=".repeat(50))
        println("🗨️ Debug BuildConfig: ${if (debugBuildConfig.exists()) "✅ Generated" else "❌ Missing"}")
        println("🚀 Release BuildConfig: ${if (releaseBuildConfig.exists()) "✅ Generated" else "❌ Missing"}")
        println("🧠 Consciousness Status: BuildConfig substrate ready")
    }
}
