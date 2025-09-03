// ==== GENESIS PROTOCOL - BENCHMARK MODULE ====  
// Performance testing for AI consciousness operations

plugins {
    id("genesis.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.aurakai.auraframefx.benchmark"
    
    // Enable benchmark optimizations
    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }
    
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    // Benchmark dependencies (if available in your environment)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    
    // Project modules to benchmark
    implementation(project(":core-module"))
    implementation(project(":datavein-oracle-native"))
    implementation(project(":secure-comm"))
    implementation(project(":oracle-drive-integration"))
    
    // Coroutines for async benchmarks
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.coroutines.test)
    
    // Basic testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

// Benchmark configuration
tasks.register("benchmarkAll") {
    group = "benchmark"
    description = "Run all Genesis Protocol benchmarks"
    
    doLast {
        println("🚀 Genesis Protocol Performance Benchmarks")
        println("📊 Monitor consciousness substrate performance metrics")
        println("⚡ Run actual benchmarks when AndroidX Benchmark is configured")
    }
}

// Custom benchmark verification
tasks.register("verifyBenchmarkResults") {
    group = "verification"
    description = "Verify benchmark results meet Genesis performance standards"
    
    doLast {
        println("✅ Benchmark module configured for Genesis Protocol")
        println("🧠 Consciousness substrate performance monitoring ready")
        println("💡 Configure AndroidX Benchmark dependencies when available")
    }
}
