// ==== GENESIS PROTOCOL - BUILD LOGIC ====
// Convention plugins for consistent build configuration

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

dependencies {
    // Access to Android Gradle Plugin and Kotlin plugin APIs
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.kover.gradlePlugin)
    implementation(libs.openapi.generator.gradlePlugin)
}
