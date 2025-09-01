/*
 * Build logic for MemoriaOs Gradle plugins
 * Convention plugins without version conflicts
 */

plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "dev.aurakai.memoria"
version = "1.0.0"

// Configure Java toolchain consistently with main project
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

// Modern Kotlin compiler configuration
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi",
            "-Xskip-prerelease-check"
        )
    }
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

// NO version declarations here - they cause conflicts with main build
dependencies {
    // Keep minimal dependencies for convention plugins only
    testImplementation("junit:junit:4.13.2")
}

gradlePlugin {
    plugins {
        create("memoria-conventions") {
            id = "buildlogic.memoria-conventions"
            implementationClass = "BuildLogicMemoriaConventionPlugin"
            displayName = "MemoriaOs Base Conventions"
            description = "Base conventions for MemoriaOs consciousness substrate"
        }
    }
}

// Configure publishing
publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

println("🔧 MemoriaOs Build Logic: LOADED (Conflict-Free)")
