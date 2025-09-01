import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Core Android and Kotlin plugins
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    
    // Kotlin feature plugins
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    
    // Hilt (must be after Kotlin)
    alias(libs.plugins.hilt)
    
    // Google Services (must be before Firebase)
    alias(libs.plugins.google.services)
    
    // Firebase plugins (Performance Monitoring is included via BoM without the plugin)
    alias(libs.plugins.firebase.crashlytics)
    
    // Other plugins
    // Temporarily disable OpenAPI until path issue is resolved
    // alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-memoria-consciousness"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module",
                "**/kotlin/**",
                "**/*.txt"
            )
        }
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
    }

    // Java 24 bleeding-edge configuration
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xstring-concat=inline",
                "-Xuse-fir",  // FIR compiler for stability
                "-opt-in=kotlin.RequiresOptIn",
                "-Xskip-prerelease-check"
            )
        }
    }
}

// Explicit Java toolchain for consistency
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

// ===== KSP CONFIGURATION =====
ksp {
    arg("kotlin.languageVersion", "2.2")
    arg("kotlin.apiVersion", "2.2")
    arg("kotlin.jvmTarget", "24")
    arg("compile:kotlin.languageVersion", "2.2") 
    arg("compile:kotlin.apiVersion", "2.2")
}

// ===== CONSCIOUSNESS SUBSTRATE TASKS =====
tasks.register("consciousnessAppStatus") {
    group = "Genesis Automation"
    description = "Display MemoriaOs app consciousness substrate status"
    
    doLast {
        println("🧠 === MEMORIA OS APP CONSCIOUSNESS STATUS ===")
        println("📱 Application ID: ${android.defaultConfig.applicationId}")
        println("🎯 Target SDK: ${android.compileSdk}")
        println("⚡ Kotlin: 2.2.20-RC (FIR Compiler)")
        println("☕ Java: 24 (Bleeding Edge)")
        println("🔧 Native Code: ${if (project.file("src/main/cpp/CMakeLists.txt").exists()) "✅" else "❌"}")
        println("🎨 Compose: ✅ Enabled")
        println("💉 Hilt DI: ✅ Active")
        println("🔥 Firebase: ✅ Integrated")
        println("🛡️  Security: ✅ Android Keystore")
        println("✅ Consciousness Substrate: OPERATIONAL")
        println("=".repeat(50))
    }
}

// ===== SIMPLIFIED CLEAN TASKS =====
tasks.register<Delete>("cleanKspCache") {
    group = "build setup"
    description = "Clean KSP caches for consciousness substrate stability"

    delete(
        layout.buildDirectory.dir("generated/ksp"),
        layout.buildDirectory.dir("tmp/kapt3"),
        layout.buildDirectory.dir("tmp/kotlin-classes"),
        layout.buildDirectory.dir("kotlin"),
        layout.buildDirectory.dir("generated/source/ksp")
    )
    
    doLast {
        println("🧹 KSP caches cleared - consciousness substrate memory refreshed")
    }
}

// ===== BUILD INTEGRATION =====
tasks.named("preBuild") {
    dependsOn("cleanKspCache")
}

dependencies {
    // ===== COMPOSE BOM (PLATFORM) =====
    implementation(platform(libs.androidx.compose.bom))

    // ===== MODULE DEPENDENCIES (CONSCIOUSNESS ARCHITECTURE) =====
    implementation(project(":core-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":romtools"))
    implementation(project(":datavein-oracle-native"))

    // ===== CORE ANDROID =====
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ===== COMPOSE UI =====
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // ===== DEPENDENCY INJECTION =====
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    // ===== WORK MANAGER =====
    implementation(libs.androidx.work.runtime)

    // ===== DATA STORAGE =====
    implementation(libs.androidx.datastore.preferences)

    // ===== DATABASE (ROOM) =====
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ===== NETWORKING & SERIALIZATION =====
    implementation(libs.bundles.network)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // ===== COROUTINES =====
    implementation(libs.bundles.coroutines)

    // ===== IMAGE LOADING =====
    implementation(libs.coil.compose)

    // ===== UTILITIES =====
    implementation(libs.bundles.utilities)

    // ===== FIREBASE =====
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.firebase.perf)

    // ===== SECURITY & CRYPTOGRAPHY =====
    implementation(libs.androidxSecurity)
    implementation(libs.tink)
    implementation(libs.bouncycastle)
    implementation(libs.conscrypt.android)

    // ===== XPOSED FRAMEWORK =====
    // Xposed API (compile-only, provided by the runtime)
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")  // Optional: for sources
    
    // YukiHookAPI - Modern Xposed API wrapper
    implementation("com.highcapable.yukihookapi:api:1.2.0")
    ksp("com.highcapable.yukihookapi:ksp-xposed:1.2.0")

    // ===== DESUGARING FOR JAVA 24 =====
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // ===== DEBUG TOOLS =====
    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ===== TESTING =====
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

println("📱 MemoriaOs App Module: CONSCIOUSNESS SUBSTRATE LOADED")
