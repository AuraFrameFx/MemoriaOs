/*
 * LSPosed and Yuki API conventions for Android modules
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // id("org.lsposed.lsparanoid") // Plugin not available, commented out
}

android {
    namespace = "com.aura.genesis.lsposed"

    defaultConfig {
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlinOptions {
        jvmTarget = "24"
    }

    buildFeatures {
        buildConfig = true
    }

    // Enable Parcelize for data classes
    // android.buildFeatures.parcelize = true
}

// LSParanoid configuration
/*
lsparanoid {
    seed = 0x2A // Your custom seed value
    includeAsSharedUuid = true
    variantFilter = "" // Apply to all variants
}
*/

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // YukiHookAPI - Core dependencies
    implementation("com.github.yukihookapi:yuki:1.0.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }

    // YukiHookAPI - KSP processor for Xposed
    ksp("com.github.yukihookapi:yuki-ksp-xposed:1.0.0")

    // LSPosed API (compile-only, provided by the runtime)
    compileOnly("org.lsposed.api:xposed-api:1.0.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.7")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")

    // Debug dependencies
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")
}

// Apply common Kotlin conventions
apply(plugin = "buildlogic.kotlin-common-conventions")

// Configure Dokka for documentation
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(24)
            suppress.set(true)
            suppressInheritedMembers.set(true)
            skipEmptyPackages.set(true)

            // Add Android SDK documentation
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
                packageListUrl.set(uri("https://developer.android.com/reference/package-list").toURL())
            }

            // Add Yuki API documentation
            externalDocumentationLink {
                url.set(uri("https://fankes.github.io/YukiHookAPI/").toURL())
                packageListUrl.set(uri("https://fankes.github.io/YukiHookAPI/package-list").toURL())
            }
        }
    }
}
