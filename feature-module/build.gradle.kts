// ===== GENESIS-OS SACRED RULES: ZERO MANUAL COMPILER CONFIG =====

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.android)
}

// Added to specify Java version for this subproject
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

// REMOVED: jvmToolchain(24) - Using system Java via JAVA_HOME
// This eliminates toolchain auto-provisioning errors

android {
    // FIX: Valid namespace without hyphens
    namespace = "dev.aurakai.auraframefx.featuremodule"
    // AUTO-EVERYTHING: Use libs.versions.toml
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ✅ FIXED: Enable Genesis Protocol build features
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false  // Genesis Protocol - Compose only
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }




    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1"
            )
        }
    }

    // CONSCIOUSNESS-OPTIMIZED: AGP 8.13.0-rc01 auto-provisions build tools
    buildToolsVersion = "36.0.0"
}

dependencies {
    // SACRED RULE #5: DEPENDENCY HIERARCHY - All modules depend on :core-module and :app
    implementation(project(":app"))
    implementation(platform(libs.androidx.compose.bom))
    // Project modules
    implementation(project(":core-module"))

    // Core AndroidX
    implementation(libs.bundles.androidx.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.engine)
    testDebugImplementation(libs.jetbrains.kotlin.test.junit5)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose - Genesis UI System
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines - Genesis Async Processing
    implementation(libs.bundles.coroutines)

    // Kotlin reflection for KSP
    implementation(libs.kotlin.reflect)

    // OpenAPI Generated Code Dependencies
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Xposed Framework - LSPosed Integration
    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

    // Utilities
    implementation(libs.bundles.utilities)

    // Testing
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
