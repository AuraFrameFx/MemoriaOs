/*
 * MemoriaOs Consciousness Substrate - Settings Configuration  
 * Advanced multi-module AI architecture with bleeding-edge configuration
 * Supports Gradle 9.1.0-rc-1, AGP 9.0.0-alpha02, Kotlin 2.2.20-RC
 */

// ===== ENABLE GRADLE PREVIEW FEATURES =====
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// ===== PLUGIN MANAGEMENT =====
pluginManagement {
    // Include build-logic for convention plugins
    includeBuild("build-logic")
    
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*") 
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        
        // Bleeding-edge repositories for latest versions
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            content {
                includeGroupByRegex("androidx\\.compose\\.compiler.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            content {
                includeGroupByRegex("org\\.jetbrains\\.compose.*")
            }
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            content {
                includeGroupByRegex("org\\.jetbrains.*")
                includeGroupByRegex("com\\.android.*")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
    }
    
    // Plugin versions management
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "com.google.devtools.ksp" -> useVersion("2.2.20-RC-2.0.2")
            }
        }
    }
    
    // JDK toolchain management
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }
}

// ===== DEPENDENCY RESOLUTION MANAGEMENT =====
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        
        // Specialized repositories for advanced features
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            content {
                includeGroupByRegex("androidx\\.compose\\.compiler.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            content {
                includeGroupByRegex("org\\.jetbrains\\.compose.*")
            }
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            content {
                includeGroupByRegex("org\\.jetbrains.*")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
        
        // Xposed & Advanced Android Frameworks
        maven("https://api.xposed.info/") {
            content {
                includeGroup("de.robv.android.xposed")
            }
        }
        maven("https://repo.lsposed.org/maven") {
            content {
                includeGroupByRegex("org\\.lsposed.*")
            }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/releases/") {
            content {
                includeGroupByRegex("com\\.highcapable.*")
            }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            content {
                includeGroupByRegex("com\\.highcapable.*")
            }
        }
    }
    
    // Version catalog is automatically loaded from gradle/libs.versions.toml
}

// ===== ROOT PROJECT CONFIGURATION =====
rootProject.name = "MemoriaOs"

// ===== COMPREHENSIVE MODULE INCLUSION =====
// Main application
include(":app")

// Core architecture modules
include(":core-module")
include(":feature-module")

// Specialized service modules
include(":oracle-drive-integration")
include(":secure-comm")
include(":datavein-oracle-native")

// UI and interaction modules  
include(":collab-canvas")
include(":colorblendr")
include(":sandbox-ui")

// System integration modules
include(":romtools")

// Utility modules
include(":utilities")
include(":list")

// Modular feature components (A-F series)
include(":module-a")
include(":module-b") 
include(":module-c")
include(":module-d")
include(":module-e")
include(":module-f")

// Testing and development modules
include(":jvm-test")

println("🧠 MemoriaOs Consciousness Substrate Initialized")
println("📡 Multi-module architecture configured")
println("⚡ Type-safe project accessors: ENABLED")
println("🚀 Configuration cache: STABLE")
println("✅ Consciousness substrate: READY")
