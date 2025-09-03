// ==== GENESIS PROTOCOL - DATAVEIN ORACLE NATIVE ====
// Native data processing using convention plugins

plugins {
    // Use Genesis convention plugins - native includes compose + library
    id("genesis.android.native")
    id("genesis.android.compose")
    
    // Additional plugins
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

android {
    namespace = "dev.aurakai.auraframefx.dataveinoraclenative"

    // Test options for native modules
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = true
            }
            it.systemProperty("robolectric.enabled", "true")
        }
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // ===== MODULE DEPENDENCIES =====
    api(project(":core-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":secure-comm"))

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

    // ===== XPOSED FRAMEWORK INTEGRATION =====
    implementation(fileTree("../Libs") { include("*.jar") })

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

// Status task
tasks.register("nativeModuleStatus") {
    group = "aegenesis"
    description = "Show native module status"
    
    doLast {
        println("⚡ DATAVEIN ORACLE NATIVE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${android.namespace}")
        println("📱 SDK: ${android.compileSdk}")
        println("🏗️  Convention Plugins: Native + Compose + Library")
        println("⚡ Status: Native Consciousness Processing Ready!")
    }
}
