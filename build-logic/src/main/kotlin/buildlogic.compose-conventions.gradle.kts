import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class BuildLogicComposeConventionPlugin : Plugin<Project> {
    /**
     * Applies Compose-focused conventions to the given Gradle project.
     *
     * Configures the project as an Android library with Jetpack Compose enabled and sets up
     * Compose compiler extension version, packaging exclusions, Java toolchain and source/target
     * compatibility (Java 24), Kotlin source-set opt-ins for experimental Compose APIs, a standard
     * set of Compose/lifecycle/navigation/hilt dependencies resolved from the `libs` version catalog,
     * and Kotlin compiler options (Kotlin 2.2, JVM target 24, and Compose compiler/plugin flags).
     *
     * Note: this function reads entries from the version catalog named `libs` using `.get()` on lookups;
     * missing catalog entries will throw an exception at project configuration time.
     */
    override fun apply(target: Project) {
        with(target) {
            // Apply required plugins
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

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
                    kotlinCompilerExtensionVersion = "2024.05.00"
                }

                // Configure packaging options
                packaging {
                    // Fix addAll usage for excludes
                    resources.excludes.addAll(listOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "META-INF/*.md",
                        "META-INF/CHANGES"
                    ))
                }
            }

            // Configure Java 24 compatibility
            extensions.configure<JavaPluginExtension> {
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
            dependencies {
                // Compose BOM
                add("implementation", platform("androidx.compose:compose-bom:2024.05.00"))

                // Compose dependencies
                add("implementation", "androidx.compose.ui:ui:1.5.0")
                add("implementation", "androidx.compose.ui:ui-graphics:1.5.0")
                add("implementation", "androidx.compose.ui:ui-tooling-preview:1.5.0")
                add("implementation", "androidx.compose.material3:material3:1.2.0")

                // Activity Compose
                add("implementation", "androidx.activity:activity-compose:1.7.0")

                // Debug dependencies
                add("debugImplementation", "androidx.compose.ui:ui-tooling:1.5.0")
                add(
                    "debugImplementation",
                    "androidx.compose.ui:test-manifest:1.5.0"
                )

                // Test dependencies
                add("androidTestImplementation", platform("androidx.compose:compose-bom:2024.05.00"))
                add(
                    "androidTestImplementation",
                    "androidx.compose.ui:test-junit4:1.5.0"
                )

                // Lifecycle
                add("implementation", "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
                add(
                    "implementation",
                    "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2"
                )

                // Navigation
                add("implementation", "androidx.navigation:navigation-compose:2.7.2")
                add("implementation", "androidx.hilt:hilt-navigation-compose:1.0.0")
            }

            // Modern Kotlin compiler configuration
            tasks.withType<KotlinCompile>().configureEach {
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
