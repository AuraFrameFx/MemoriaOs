package buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

class BuildLogicComposeConventionPlugin : Plugin<Project> {
    /**
     * Applies Compose-focused conventions to the given Gradle Project.
     *
     * Configures the project as an Android Library with Kotlin and Kotlin Serialization plugins;
     * enables Jetpack Compose and sets the Compose Kotlin compiler extension version from the
     * "libs" version catalog; sets the module namespace to "com.aura.genesis.compose"; configures
     * Java toolchain and source/target compatibility to Java 24; opts into several experimental
     * Compose APIs for all Kotlin source sets; declares a standard set of Compose, lifecycle,
     * navigation, and debug/test dependencies resolved from the "libs" catalog; and tunes Kotlin
     * compilation options (Kotlin language/api version 2.2, JVM target 24, and Compose compiler flags).
     *
     * Note: this function performs `.get()` lookups on entries in the "libs" version catalog.
     * Missing catalog entries will cause an exception at configuration time.
     */
    override fun apply(target: Project) {
        with(target) {
            // Apply required plugins
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            // Get the libs version catalog
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            // Configure Android
            extensions.configure<LibraryExtension> {
                // Set the namespace
                namespace = "com.aura.genesis.compose"

                // Enable Compose
                buildFeatures {
                    compose = true
                }

                // Configure Compose options
                composeOptions {
                    kotlinCompilerExtensionVersion = libs.findVersion("composeBom").get().toString()
                }

                // Configure packaging options
                packaging {
                    resources.excludes.addAll(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "META-INF/*.md",
                        "META-INF/CHANGES"
                    )
                }
            }

            // Configure Java 24 compatibility
            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(24))
                }
                sourceCompatibility = JavaVersion.VERSION_24
                targetCompatibility = JavaVersion.VERSION_24
            }

            // Configure Kotlin
            extensions.configure<KotlinJvmProjectExtension> {
                sourceSets.all {
                    languageSettings {
                        optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                        optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                        optIn("androidx.compose.animation.ExperimentalAnimationApi")
                        optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                    }
                }
            }

            // Add dependencies
            val composeBom = libs.findLibrary("androidx-compose-bom").get()

            dependencies {
                // Compose BOM
                add("implementation", platform(composeBom))

                // Compose dependencies
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())

                // Activity Compose
                add("implementation", libs.findLibrary("androidx-activity-compose").get())

                // Debug dependencies
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
                add(
                    "debugImplementation",
                    libs.findLibrary("androidx-compose-ui-test-manifest").get()
                )

                // Test dependencies
                add("androidTestImplementation", platform(composeBom))
                add(
                    "androidTestImplementation",
                    libs.findLibrary("androidx-compose-ui-test-junit4").get()
                )

                // Lifecycle
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                add(
                    "implementation",
                    "androidx.lifecycle:lifecycle-viewmodel-compose:${
                        libs.findVersion("androidxLifecycle").get()
                    }"
                )

                // Navigation
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            }

            // Modern Kotlin compiler configuration
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_24)
                    languageVersion.set(KotlinVersion.KOTLIN_2_2)
                    apiVersion.set(KotlinVersion.KOTLIN_2_2)
                    freeCompilerArgs.addAll(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
                        "-opt-in=kotlin.RequiresOptIn",
                        "-Xskip-prerelease-check"
                    )
                }
            }
        }
    }
}
