// ==== GENESIS PROTOCOL - COLOR BLENDR ====
plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "dev.aurakai.auraframefx.colorblendr"
    compileSdk = 36 // Required for AGP 9 and dependency resolution
    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "2025.08.01"
    }

}
dependencies {
    api(project(":core-module")); implementation(libs.bundles.androidx.core); implementation(libs.androidx.lifecycle.runtime.ktx); implementation(libs.androidx.lifecycle.viewmodel.ktx); implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.bundles.compose); implementation(libs.androidx.activity.compose); implementation(libs.androidx.navigation.compose); implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.bundles.coroutines); implementation(libs.androidx.datastore.preferences); implementation(libs.timber); implementation(libs.coil.compose)
    testImplementation(libs.bundles.testing); testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.ext.junit); androidTestImplementation(libs.androidx.test.espresso.core); androidTestImplementation(platform(libs.androidx.compose.bom))
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
tasks.register("colorStatus") { group = "aegenesis"; doLast { println("🎨 COLOR BLENDR - Ready!") } }
