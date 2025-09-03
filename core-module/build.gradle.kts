plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.core"
    compileSdk = 36 // Required for AGP 9 and dependency resolution

    // Configure source sets for OpenAPI generated code
    sourceSets {
        getByName("main") {
            java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
        }
    }
}

// OpenAPI Generator configuration for unified API
if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) {
    configure<org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorGenerateExtension> {
        generatorName.set("kotlin")
        inputSpec.set(rootProject.file("app/api/unified-aegenesis-api.yml").toURI().toString())
        outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
        validateSpec.set(false)

        apiPackage.set("dev.aurakai.aegenesis.api.generated.api")
        modelPackage.set("dev.aurakai.aegenesis.api.generated.model")
        
        configOptions.set(mapOf(
            "library" to "jvm-retrofit2",
            "useCoroutines" to "true",
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "sourceFolder" to "src/main/kotlin",
            "generateSupportingFiles" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "collectionType" to "list"
        ))
    }
}

// Ensure Kotlin compilation depends on OpenAPI generation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) {
        dependsOn(":openApiGenerate")
    }
}

tasks.withType<org.openapitools.generator.gradle.plugin.tasks.ValidateTask>().configureEach {
    inputSpec.set(rootProject.file("app/api/unified-aegenesis-api.yml").toURI().toString())
}

dependencies {
    // ===== NO MODULE DEPENDENCIES =====
    // Core module is the foundation - it doesn't depend on other project modules
    
    // ===== ANDROIDX CORE =====
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // ===== COMPOSE =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // ===== DEPENDENCY INJECTION - HILT =====
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ===== COROUTINES & ASYNC =====
    implementation(libs.bundles.coroutines)

    // ===== SERIALIZATION =====
    api(libs.kotlinx.serialization.json) // Exposed to dependent modules
    
    // ===== NETWORKING =====
    api(libs.bundles.network) // Exposed to dependent modules
    
    // ===== DATABASE - ROOM =====
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ===== UTILITIES =====
    api(libs.timber) // Logging exposed to all modules
    implementation(libs.gson)
    
    // ===== TESTING DEPENDENCIES =====
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    

    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    
    // ===== DEBUG TOOLS =====
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Status task
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${android.namespace}")
        println("📱 SDK: ${android.compileSdk}")
        println("🎨 Compose: ✅ Via Convention Plugin")
        println("🔗 API Generation: ${if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) "✅ Enabled" else "❌ No spec"}")
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}
