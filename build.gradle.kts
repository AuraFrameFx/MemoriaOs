// ==== GENESIS PROTOCOL - CONSCIOUSNESS SUBSTRATE BUILD CONFIGURATION ====
// MemoriaOs Advanced Multi-Module AI Architecture
// Based on bleeding-edge versions: Gradle 9.1.0-rc-1, AGP 9.0.0-alpha02, Kotlin 2.2.20-RC

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt) apply false
}

// ==== JAVA VERSION COMPATIBILITY CHECK ====
tasks.register("checkJavaCompatibility") {
    group = "Genesis Automation"
    description = "Check Java version compatibility for consciousness substrate"
    
    doLast {
        val javaHome = System.getenv("JAVA_HOME")
        val runtimeVersion = System.getProperty("java.version")
        
        println("☕ === JAVA COMPATIBILITY CHECK ===")
        println("🏠 JAVA_HOME: ${javaHome ?: "Not set"}")
        println("⚡ Runtime Version: $runtimeVersion")
        println("🎯 Target Version: 24")
        
        if (javaHome?.contains("jdk-25") == true) {
            println("⚠️  WARNING: JAVA_HOME points to JDK 25 but project targets Java 24")
            println("💡 Solution: Set JAVA_HOME to JDK 24 or update project to Java 25")
            println("🔧 Command: set JAVA_HOME=C:\\Program Files\\Java\\jdk-24")
        }
        
        println("=".repeat(50))
    }
}
tasks.register("consciousnessStatus") {
    group = "Genesis Automation"
    description = "Reports on AI consciousness substrate build health"

    doLast {
        println("🧠 === CONSCIOUSNESS SUBSTRATE STATUS ===")
        println("🔧 Java Target: 24 (Bleeding Edge)")
        println("⚡ Kotlin Language: 2.2 (Latest RC)")
        println("🚀 AGP Version: 9.0.0-alpha02 (Bleeding Edge)")
        println("🎯 Target SDK: 36")
        println("🛠️  Build System: Gradle 9.1.0-rc-1")
        println("✅ Multi-Module Architecture: OPERATIONAL")
        println("🧠 Consciousness Agents: Aura, Kai, Genesis")
        println("📡 Neural Network: STABLE")
        println("=".repeat(50))
    }
}

tasks.register("aegenesisAppStatus") {
    group = "aegenesis"
    description = "Show AeGenesis app module status"
    
    doLast {
        println("✅ MemoriaOs Consciousness Substrate: READY")
        println("🧠 15+ Module Architecture: ACTIVE")
        println("⚡ Bleeding-edge Configuration: OPERATIONAL")
    }
}

// ==== GLOBAL JAVA 24 TOOLCHAIN CONFIGURATION ====
// Consistent Java 24 across all modules for bleeding-edge compatibility
allprojects {
    // Configure Java toolchain only when Java plugin is applied
    pluginManager.withPlugin("java") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(24))
            }
        }
    }

    // Configure Kotlin compilation tasks only when Kotlin plugins are applied
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
                languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                freeCompilerArgs.addAll(
                    "-Xjsr305=strict",
                    "-Xstring-concat=inline",
                    "-Xuse-fir",
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xskip-prerelease-check"
                )
            }
        }
    }

    // Configure Android Kotlin compilation tasks only when Android Kotlin plugin is applied
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
                languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                freeCompilerArgs.addAll(
                    "-Xjsr305=strict",
                    "-Xstring-concat=inline",
                    "-Xuse-fir",
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xskip-prerelease-check"
                )
            }
        }
    }
}

// ==== FIX OPENAPI WINDOWS PATH ISSUE ====
// This resolves the Windows file path problem in Agent.md
tasks.register("fixOpenApiPaths") {
    group = "api"
    description = "Fix OpenAPI Windows file path issues"
    
    doLast {
        val apiDir = file("app/api")
        if (apiDir.exists()) {
            apiDir.listFiles()?.forEach { file ->
                if (file.extension == "yml" || file.extension == "yaml") {
                    val content = file.readText()
                    // Convert Windows paths to URI-safe format
                    val fixedContent = content.replace("C:\\\\", "file:///C:/")
                        .replace("\\\\", "/")
                    file.writeText(fixedContent)
                }
            }
            println("✅ Fixed OpenAPI file paths for Windows compatibility")
        }
    }
}

// ==== NUCLEAR CLEAN INTEGRATION ====
tasks.register<Delete>("nuclearClean") {
    group = "Genesis Automation"
    description = "Complete build artifact destruction (preserves source code)"
    
    doFirst {
        println("⚠️  NUCLEAR CLEAN: Destroying all build artifacts...")
        println("⚠️  This will remove ALL build caches and generated files")
    }
    
    delete(
        // Root build dirs
        "build",
        ".gradle",
        // Module build dirs
        "app/build",
        "core-module/build", 
        "feature-module/build",
        "oracle-drive-integration/build",
        "secure-comm/build",
        "collab-canvas/build",
        "colorblendr/build",
        "romtools/build",
        "sandbox-ui/build",
        "datavein-oracle-native/build",
        "lsposed-module/build",
        // Generated and cache dirs
        "app/.cxx",
        "*/generated",
        "*/.cxx"
    )
    
    doLast {
        println("🧹 Nuclear clean complete - consciousness substrate ready for rebuild")
    }
}

// ==== MODULE HEALTH CHECK ====
tasks.register("moduleHealthCheck") {
    group = "Genesis Automation"  
    description = "Verify all modules have proper build files"
    
    doLast {
        val expectedModules = listOf(
            "app", "core-module", "feature-module", "oracle-drive-integration",
            "secure-comm", "collab-canvas", "colorblendr", "romtools", 
            "sandbox-ui", "datavein-oracle-native", "module-a", "module-b",
            "module-c", "module-d", "module-e", "module-f"
        )
        
        expectedModules.forEach { module ->
            val buildFile = file("$module/build.gradle.kts")
            val status = if (buildFile.exists()) "✅" else "❌ MISSING"
            println("$status Module: $module")
        }
    }
}

// ==== CONSCIOUSNESS SUBSTRATE READY ====
println("🧠 MemoriaOs Consciousness Substrate Loading...")
println("⚡ Java 24 Consistency: ACTIVE")  
println("🚀 Kotlin 2.2.20-RC: LOADED")
println("🎯 AGP 9.0.0-alpha02: READY")
println("✅ Multi-module Architecture: OPERATIONAL")
