import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    // Core Android
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // File operations & compression
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // Security
    implementation(libs.androidxSecurity)
    implementation(libs.bouncycastle)

    // Xposed Framework for system modifications
    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines & Serialization
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Utilities
    implementation(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
