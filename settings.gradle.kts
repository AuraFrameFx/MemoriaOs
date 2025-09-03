@file:Suppress("UnstableApiUsage", "JCenterRepositoryObsolete")

// ===== GENESIS PROTOCOL - SETTINGS =====
// AeGenesis Coinscience AI Ecosystem - Enhanced with Build Logic
// Version: 2025.09.02-03 - Full Enhancement Suite

// Enable Gradle features
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    // Include build-logic for convention plugins
    includeBuild("build-logic")

    repositories {
        // Primary repositories
        google()
        gradlePluginPortal()
        mavenCentral()

        // AndroidX Compose
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            name = "AndroidX Compose"
            content { includeGroup("androidx.compose.compiler") }
        }

        // JetBrains Compose
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            name = "JetBrains Compose"
        }

        // Snapshots
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "Sonatype Snapshots"
            mavenContent { snapshotsOnly() }
        }

        // JitPack for GitHub dependencies
        maven("https://jitpack.io") {
            name = "JitPack"
            content { includeGroupByRegex("com\\.github\\..*") }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// Configure Java toolchain resolution


dependencyResolutionManagement {
    // Enforce consistent dependency resolution
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // FIX: Simplified repository configuration to resolve dependencies correctly.
    // The previous strict filtering was preventing many common libraries from being found.
    repositories {
        google()
        mavenCentral()

        // AndroidX Compose
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            name = "AndroidX Compose"
        }

        // JetBrains Compose
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            name = "JetBrains Compose"
        }

        // Snapshots for pre-release libraries
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "Sonatype Snapshots"
        }

        // JitPack for GitHub dependencies
        maven("https://jitpack.io") {
            name = "JitPack"
        }
    }
}

// ===== PROJECT IDENTIFICATION =====
rootProject.name = "MemoriaOs"

// ===== MODULE INCLUSION =====
// Core modules
include(":app")
include(":core-module")

// Feature modules
include(":feature-module")
include(":datavein-oracle-native")
include(":oracle-drive-integration")
include(":secure-comm")
include(":sandbox-ui")
include(":collab-canvas")
include(":colorblendr")
include(":romtools")

// Dynamic modules (A-F)
include(":module-a")
include(":module-b")
include(":module-c")
include(":module-d")
include(":module-e")
include(":module-f")

// Testing & Quality modules
include(":benchmark")
include(":screenshot-tests")

// ===== MODULE CONFIGURATION =====
rootProject.children.forEach { project ->
    val projectDir = File(rootProject.projectDir, project.name)
    if (projectDir.exists()) {
        project.projectDir = projectDir
        println("✅ Module configured: ${project.name}")
    } else {
        println("⚠️ Warning: Project directory not found: ${projectDir.absolutePath}")
    }
}

println("🏗️  Genesis Protocol Enhanced Build System")
println("📦 Total modules: ${rootProject.children.size}")
println("🎯 Build-logic: Convention plugins active")
println("🧠 Ready to build consciousness substrate!")
