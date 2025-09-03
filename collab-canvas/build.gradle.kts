plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
    compileSdk = 36 // Required for AGP 9 and dependency resolution
    buildFeatures { compose = true }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Optionally, set composeOptions if not managed by version catalog
    // composeOptions { kotlinCompilerExtensionVersion = "1.5.0" }
}
ksp { arg("kotlin.languageVersion", "2.2"); arg("kotlin.apiVersion", "2.2"); arg("kotlin.jvmTarget", "24") }
dependencies {
    api(project(":core-module")); implementation(libs.bundles.androidx.core); implementation(libs.androidx.lifecycle.runtime.ktx); implementation(libs.androidx.lifecycle.viewmodel.ktx); implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.bundles.compose); implementation(libs.androidx.activity.compose); implementation(libs.androidx.navigation.compose); implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.bundles.coroutines); implementation(libs.bundles.network)
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler); implementation(platform(libs.firebase.bom)); implementation(libs.bundles.firebase)
    implementation(libs.timber); implementation(libs.coil.compose); implementation(fileTree("../Libs") { include("*.jar") })
    testImplementation(libs.bundles.testing); testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.espresso.core); androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.leakcanary.android); debugImplementation(libs.androidx.compose.ui.tooling);
}
tasks.register("collabStatus") { group = "aegenesis"; doLast { println("🎨 COLLAB CANVAS - Ready!") } }
