import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.aurakai.auraframefx.datavein"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // NDK configuration for native data processing
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
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

    // External native build if CMakeLists.txt exists
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

ksp {
    arg("kotlin.languageVersion", "2.2")
    arg("kotlin.apiVersion", "2.2")
    arg("kotlin.jvmTarget", "24")
}

dependencies {
    // Module dependencies
    implementation(project(":core-module"))
    implementation(project(":secure-comm"))
    implementation(project(":oracle-drive-integration"))

    // Core Android
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Data processing & storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // File operations
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // Security
    implementation(libs.androidxSecurity)
    implementation(libs.tink)
    implementation(libs.bouncycastle)

    // Networking
    implementation(libs.bundles.network)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines & Serialization
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Utilities
    implementation(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
