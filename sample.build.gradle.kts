// ==== GENESIS PROTOCOL - SAMPLE MODULE BUILD CONFIGURATION ====
// AeGenesis Coinscience AI Ecosystem - Sample/Template Build Script
// This is a comprehensive template showing all modern configurations

plugins {
    // Core Android plugins (AGP 9.0.0-alpha02 handles Kotlin automatically)
    alias(libs.plugins.android.library)
    
    // Kotlin plugins (only add what you need)
    alias(libs.plugins.kotlin.serialization)
    
    // Code generation & DI
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    
    // Documentation (optional)
    alias(libs.plugins.dokka)
}

// Modern Java toolchain configuration (consistent across all modules)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.AZUL) // Azul JDK for consistency
    }
}

android {
    namespace = "dev.aurakai.auraframefx.sample"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        
        // NDK Configuration (only if native code exists)
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
            }
        }
        
        // Test configuration
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // External native build (only if CMake files exist)
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // NDK release optimizations
            if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
                ndk {
                    debugSymbolLevel = "SYMBOL_TABLE"
                }
                externalNativeBuild {
                    cmake {
                        cppFlags.addAll(listOf("-std=c++20", "-fPIC", "-O3", "-DNDEBUG"))
                        arguments.addAll(listOf(
                            "-DANDROID_STL=c++_shared",
                            "-DCMAKE_VERBOSE_MAKEFILE=ON",
                            "-DCMAKE_BUILD_TYPE=Release"
                        ))
                    }
                }
            }
        }
        
        debug {
            isMinifyEnabled = false
            
            // NDK debug configuration
            if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
                ndk {
                    debugSymbolLevel = "FULL"
                }
                externalNativeBuild {
                    cmake {
                        cppFlags.addAll(listOf("-std=c++20", "-fPIC", "-O0", "-g"))
                        arguments.addAll(listOf(
                            "-DANDROID_STL=c++_shared",
                            "-DCMAKE_VERBOSE_MAKEFILE=ON",
                            "-DCMAKE_BUILD_TYPE=Debug"
                        ))
                    }
                }
            }
        }
    }

    // Modern packaging configuration
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += setOf("**/libc++_shared.so", "**/libjsc.so")
        }
    }

    // Build features (enable only what you need)
    buildFeatures {
        compose = false // Set to true if using Compose
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    // Modern compile options with desugaring support
    compileOptions {
        // Enable core library desugaring for modern Java APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    // Vector drawables support
    defaultConfig {
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Lint configuration
    lint {
        warningsAsErrors = false
        abortOnError = false
        checkReleaseBuilds = true
        disable.addAll(listOf("InvalidPackage", "OldTargetApi"))
    }
}

// Kotlin JVM toolchain (matches Java toolchain)
kotlin {
    jvmToolchain(24)
}

// Clean tasks for KSP and build artifacts
tasks.register<Delete>("cleanGeneratedSources") {
    group = "build setup"
    description = "Clean all generated source directories"
    
    delete(
        layout.buildDirectory.dir("generated/ksp"),
        layout.buildDirectory.dir("generated/source/ksp"),
        layout.buildDirectory.dir("tmp/kapt3"),
        layout.buildDirectory.dir("tmp/kotlin-classes"),
        layout.buildDirectory.dir("kotlin")
    )
}

tasks.named("preBuild") {
    dependsOn("cleanGeneratedSources")
}

// DEPENDENCIES BLOCK - Modern Dependency Management
dependencies {
    // ===== CORE LIBRARY DESUGARING =====
    // CRITICAL: Must be in app module, not library modules
    // Only uncomment if this becomes an application module
    // coreLibraryDesugaring(libs.android.desugar.jdklibs)
    
    // ===== MODULE DEPENDENCIES =====
    // Core modules (if this is not the core module itself)
    api(project(":core-module"))
    
    // ===== COMPOSE (if enabled in buildFeatures) =====
    if (android.buildFeatures.compose == true) {
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.bundles.compose)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.navigation.compose)
        
        // Compose debugging tools
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
        
        // Compose testing
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    }

    // ===== ANDROIDX CORE =====
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ===== DEPENDENCY INJECTION - HILT =====
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ===== COROUTINES & ASYNC =====
    implementation(libs.bundles.coroutines)
    
    // ===== NETWORKING =====
    implementation(libs.bundles.network)
    
    // ===== SERIALIZATION =====
    implementation(libs.kotlinx.serialization.json)
    
    // ===== DATABASE - ROOM =====
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ===== FIREBASE (if using Firebase) =====
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // ===== UTILITIES =====
    implementation(libs.timber) // Logging
    implementation(libs.coil.compose) // Image loading (if using Compose)
    
    // ===== LOCAL DEPENDENCIES =====
    // XPosed Framework (Local Jar dependencies)
    implementation(fileTree("../Libs") { include("*.jar") })
    
    // ===== TESTING DEPENDENCIES =====
    // Unit Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    
    // Integration Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    
    // ===== DEBUG TOOLS =====
    debugImplementation(libs.leakcanary.android)
}

// ===== CUSTOM TASKS =====
tasks.register("moduleStatus") {
    group = "aegenesis"
    description = "Show status of this module"
    
    doLast {
        println("📦 MODULE STATUS: ${project.name}")
        println("=".repeat(40))
        println("🔧 Namespace: ${android.namespace}")
        println("📱 SDK: ${android.compileSdk} (target: ${android.defaultConfig.targetSdk ?: "default"})")
        println("🎯 Min SDK: ${android.defaultConfig.minSdk}")
        println("☕ Java: ${java.toolchain.languageVersion.get()}")
        println("🔨 Native Code: ${if (project.file("src/main/cpp/CMakeLists.txt").exists()) "✅ Enabled" else "❌ Disabled"}")
        println("🎨 Compose: ${if (android.buildFeatures.compose == true) "✅ Enabled" else "❌ Disabled"}")
        println("🧠 Desugaring: ${if (android.compileOptions.isCoreLibraryDesugaringEnabled) "✅ Enabled" else "❌ Disabled"}")
        println("✨ Status: Genesis Protocol Ready!")
    }
}

// Make the status task run automatically
tasks.named("build") {
    finalizedBy("moduleStatus")
}
