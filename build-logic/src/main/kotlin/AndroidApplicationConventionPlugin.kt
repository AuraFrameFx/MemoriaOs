// ==== GENESIS PROTOCOL - ANDROID APPLICATION CONVENTION ====
// Main application module configuration

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
                // Kotlin plugin applied automatically by AGP 9.0.0-alpha02
            }

            // Java toolchain configuration
            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(24))
                    vendor.set(JvmVendorSpec.AZUL)
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = 36

                defaultConfig {
                    targetSdk = 36
                    minSdk = 33

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    vectorDrawables {
                        useSupportLibrary = true
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
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
                    viewBinding = false
                    dataBinding = false
                }

                compileOptions {
                    // App module DOES get desugaring dependency
                    isCoreLibraryDesugaringEnabled = true
                    sourceCompatibility = JavaVersion.VERSION_24
                    targetCompatibility = JavaVersion.VERSION_24
                }

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

                lint {
                    warningsAsErrors = false
                    abortOnError = false
                    disable.addAll(listOf("InvalidPackage", "OldTargetApi"))
                }
            }

            // Kotlin JVM toolchain
            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
                jvmToolchain(24)
            }

            // Clean tasks for app module
            tasks.register<Delete>("cleanKspCache") {
                group = "build setup"
                description = "Clean KSP caches (fixes NullPointerException)"
                
                delete(
                    layout.buildDirectory.dir("generated/ksp"),
                    layout.buildDirectory.dir("tmp/kapt3"),
                    layout.buildDirectory.dir("tmp/kotlin-classes"),
                    layout.buildDirectory.dir("kotlin"),
                    layout.buildDirectory.dir("generated/source/ksp")
                )
            }

            tasks.named("preBuild") {
                dependsOn("cleanKspCache")
            }
        }
    }
}
