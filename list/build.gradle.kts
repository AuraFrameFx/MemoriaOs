import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

group = "dev.aurakai.auraframefx.list"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
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

dependencies {
    // Pure Kotlin JVM module - no Android dependencies
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Data structures and collections utilities
    implementation(libs.kotlinx.datetime)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}
